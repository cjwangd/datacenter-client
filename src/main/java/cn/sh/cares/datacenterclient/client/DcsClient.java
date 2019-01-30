package cn.sh.cares.datacenterclient.client;

import cn.sh.cares.datacenterclient.common.MqMessageConstant;
import cn.sh.cares.datacenterclient.message.MqMessage;
import cn.sh.cares.datacenterclient.message.MqMessageBody;
import cn.sh.cares.datacenterclient.message.MqMessageBuilder;
import cn.sh.cares.datacenterclient.message.MqMessageHeader;
import cn.sh.cares.datacenterclient.message.auth.AuthMessage;
import cn.sh.cares.datacenterclient.message.auth.AuthMessageBody;
import cn.sh.cares.datacenterclient.message.auth.AuthMessageHeader;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.concurrent.ThreadPoolExecutorFactoryBean;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 数据中心java 客户端
 */
@Scope("singleton")
public class DcsClient implements InitializingBean {

    public static final String APPLICATION_XML_VALUE = "application/xml";
    private static OkHttpClient okHttpClient = new OkHttpClient();
    private static MediaType mediaType = MediaType.parse(APPLICATION_XML_VALUE);
    /**
     * 系统代码
     **/
    private String sysCode = "";
    /**
     * 数据类型
     **/
    private List<String> datatypes;

    private String token;

    /**
     * 用户名
     **/
    private String username;

    /**
     * 密码
     **/
    private String password;
    /**
     * 数据中心连接地址
     **/
    private String url;
    /**
     * 每秒发送请求数
     **/
    private List<MqMessage> msgs;

    private IMsgResolver msgResolver;
    private ExecutorService executorService;

    private Logger logger = LoggerFactory.getLogger(DcsClient.class);
    private JAXBContext context;
    private Marshaller marshaller;
    private Unmarshaller unmarshaller;

    {
        ThreadPoolExecutorFactoryBean executorFactoryBean = new ThreadPoolExecutorFactoryBean();
        executorFactoryBean.setCorePoolSize(8);
        executorFactoryBean.setMaxPoolSize(20);
        executorFactoryBean.setQueueCapacity(100);
        executorFactoryBean.setThreadNamePrefix("DCS::CLIENT");
        executorFactoryBean.afterPropertiesSet();
        executorService=executorFactoryBean.getObject();

        try {
            context = JAXBContext.newInstance(
                    MqMessage.class,
                    MqMessageHeader.class,
                    MqMessageBody.class, AuthMessageHeader.class, AuthMessageBody.class, AuthMessage.class);
            marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
            unmarshaller = context.createUnmarshaller();
            unmarshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
        } catch (Exception e) {

        }

    }

    private DcsClient(){}
    private static DcsClient client = null;

    public static synchronized DcsClient getClient() {
        if (null == client) {
            client = new DcsClient();
        }
        return client;
    }


    @Override
    public void afterPropertiesSet() {

        Assert.notNull(msgResolver, "消息解析器不能为空");
        Assert.notNull(url, "数据中心连接地址不能为空");
        Assert.notNull(sysCode, "系统代码不能为空");

        msgs = new ArrayList<>(10);
        msgs.add(new MqMessageBuilder()
                .msgType(MqMessageConstant.MsgType.HEARTBEAT_REQUEST)
                .receiver(MqMessageConstant.Participate.DATACENTER.getParticipate())
                .sendTime(new Date())
                .sender(sysCode)
                .token(token)
                .build());
        datatypes.stream().forEach(d -> {
            msgs.add(new MqMessageBuilder()
                    .msgType(MqMessageConstant.MsgType.SUBSCRIBE_REQUEST)
                    .receiver(MqMessageConstant.Participate.DATACENTER.getParticipate())
                    .sendTime(new Date())
                    .sender(sysCode)
                    .token(token)
                    .dataType(d)
                    .build());
        });
        login();

        if (StringUtils.isNotEmpty(token)) {
            executorService.submit(new DataRequestThread());
        } else {
            logger.debug("登录数据中心失败，请检查");
            System.exit(-2);
        }

    }


    private void sendRequest(Request request) throws Exception {
        if (null != okHttpClient && null != request) {
            //执行请求，得到响应
            Response response = okHttpClient.newCall(request).execute();
            if (HttpServletResponse.SC_OK == response.code()) {
                String resp = response.body().string();
                logger.debug("收到数据中心响应消息::{}", resp);
                if (StringUtils.isNotEmpty(resp)) {
                    MqMessage mqMessage = (MqMessage) unmarshaller.unmarshal(new StreamSource(resp));
                    if (null == mqMessage) {
                        return;
                    }else if (MqMessageConstant.MqMessageStatus.TOKENEXPIRE.getStatus().equals(mqMessage.getBody().getStatus())) {
                        login();
                    } else {
                        msgResolver.resolve(mqMessage);
                    }
                }
            }
        }
    }

    /**
     * 数据订阅线程
     */
    public class DataRequestThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                msgs.stream().forEach(m -> {
                    try {
                        m.getBody().setSeqNum(msgResolver.getUniqueSeq());
                        StringWriter writer = new StringWriter();
                        marshaller.marshal(m, writer);
                        logger.debug("发送请求::{}", writer.toString());
                        RequestBody requestBody = RequestBody.create(mediaType, writer.toString());
                        Request request = new Request.Builder()
                                .url(url)
                                .post(requestBody)
                                .build();
                        executorService.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    sendRequest(request);
                                } catch (Exception ex) {

                                }
                            }
                        });

                        Thread.sleep(1000);
                    } catch (Exception ex) {

                    }
                });
            }
        }

    }


    // 参数设置
    public void setSysCode(String sysCode) {
        this.sysCode = sysCode;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setMsgResolver(IMsgResolver msgResolver) {
        this.msgResolver = msgResolver;
    }

    public void setDatatypes(List<String> datatypes) {
        this.datatypes = datatypes;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void login() {
        AuthMessage authMessage = new AuthMessage();
        AuthMessageHeader authMessageHeader = new AuthMessageHeader();
        AuthMessageBody authMessageBody = new AuthMessageBody();
        authMessageHeader.setReceiver(MqMessageConstant.Participate.DATACENTER.getParticipate());
        authMessageHeader.setSender(sysCode);
        authMessageHeader.setSendTime(new Date());
        authMessageBody.setUserName(username);
        authMessageBody.setPassWord(password);
        authMessage.setBody(authMessageBody);
        authMessage.setHeader(authMessageHeader);

        try {
            StringWriter writer = new StringWriter();
            marshaller.marshal(authMessage, writer);
            logger.debug("发送请求::{}", writer.toString());
            RequestBody requestBody = RequestBody.create(mediaType, writer.toString());
            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();
            if (null != okHttpClient && null != request) {
                //执行请求，得到响应
                Response response = okHttpClient.newCall(request).execute();
                if (HttpServletResponse.SC_OK == response.code()) {
                    String resp = response.body().string();

                    if (StringUtils.isNotEmpty(resp)) {
                        AuthMessage authresp = (AuthMessage) unmarshaller.unmarshal(new StreamSource(resp));
                        token = authresp.getBody().getToken();
                        if (StringUtils.isEmpty(token)) {
                            logger.debug("登录失败");
                        } else {
                            logger.debug("登录成功，收到数据中心认证token::{}", token);
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }

    }
}
