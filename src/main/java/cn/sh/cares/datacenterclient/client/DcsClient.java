package cn.sh.cares.datacenterclient.client;

import cn.sh.cares.datacenterclient.common.MqMessageConstant;
import cn.sh.cares.datacenterclient.message.MqMessage;
import cn.sh.cares.datacenterclient.message.MqMessageBody;
import cn.sh.cares.datacenterclient.message.MqMessageBuilder;
import cn.sh.cares.datacenterclient.message.MqMessageHeader;
import cn.sh.cares.datacenterclient.message.auth.AuthMessage;
import cn.sh.cares.datacenterclient.message.auth.AuthMessageBody;
import cn.sh.cares.datacenterclient.message.auth.AuthMessageHeader;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
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
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * 数据中心java 客户端
 */
@Scope("singleton")
public class DcsClient implements InitializingBean {

    public static final String APPLICATION_XML_VALUE = "application/xml;charset=UTF-8";
    private static OkHttpClient okHttpClient = new OkHttpClient();
    private static MediaType mediaType = MediaType.parse(APPLICATION_XML_VALUE);
    private static MediaType mediaTypeJSON = MediaType.parse("application/json;charset=UTF-8");

    /**
     * 系统代码
     **/
    private String sysCode = "";
    /**
     * 数据类型
     **/
    private String datatypes;

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
     * 心跳间隔
     */
    private long hearbeat_inteval = 1000;

    /**
     * 数据请求间隔
     */
    private long datareq_inteval = 1000;

    private List<MqMessage> msgs;

    private IMsgResolver msgResolver;

    private ExecutorService executorService;

    private Logger logger = LoggerFactory.getLogger(DcsClient.class);

    {
        ThreadPoolExecutorFactoryBean executorFactoryBean = new ThreadPoolExecutorFactoryBean();
        executorFactoryBean.setCorePoolSize(8);
        executorFactoryBean.setMaxPoolSize(20);
        executorFactoryBean.setQueueCapacity(100);
        executorFactoryBean.setThreadNamePrefix("DCS::CLIENT");
        executorFactoryBean.afterPropertiesSet();
        executorService = executorFactoryBean.getObject();
    }

    private DcsClient() {
    }

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
        Assert.notNull(username, "接入用户名不能为空");
        Assert.notNull(password, "接入密码能为空");
        Assert.notNull(datatypes, "订阅数据不能为空");



    }

    public void start() {
        // 登录数据共享平台
        login();

        // 发送订阅消息
        if (!subscribe()) {
            logger.error("发送订阅失败");
            System.exit(-9);
        }

        if (StringUtils.isNotEmpty(token)) {
            executorService.submit(new HeartBeatRequestThread(new MqMessageBuilder()
                    .msgType(MqMessageConstant.MsgType.HEARTBEAT_REQUEST)
                    .receiver(MqMessageConstant.Participate.DATACENTER.getParticipate())
                    .sendTime(new Date())
                    .sender(sysCode)
                    .token(token)
                    .build()));

            executorService.submit(new DataRequestThread(new MqMessageBuilder()
                    .msgType(MqMessageConstant.MsgType.DATA_REQUEST)
                    .receiver(MqMessageConstant.Participate.DATACENTER.getParticipate())
                    .sendTime(new Date())
                    .sender(sysCode)
                    .token(token)
                    .dataType(datatypes)
                    .build()));
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
                    JAXBContext jaxbContext = JAXBContext.newInstance(MqMessage.class,
                            MqMessageHeader.class, MqMessageBody.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    MqMessage mqMessage = (MqMessage) unmarshaller.unmarshal(
                            new StreamSource(new ByteArrayInputStream(resp.getBytes())));
                    if (null == mqMessage) {
                        return;
                    } else if (MqMessageConstant.MqMessageStatus.TOKENEXPIRE
                            .getStatus().equals(mqMessage.getBody().getStatus())) {
                        synchronized (token) {
                            token = null;
                            login();
                        }
                    } else {
                        msgResolver.resolve(resp);
                    }
                }
            }
        }
    }


    /**
     * 心跳线程
     */
    public class HeartBeatRequestThread implements Runnable {
        private MqMessage heart;

        public HeartBeatRequestThread(MqMessage heart) {
            this.heart = heart;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String msg = getMsgXml(heart);
                    logger.debug("发送心跳请求::{}", msg);
                    executorService.submit(() -> {
                        try {
                            sendRequest(getHttpReq(msg));
                        } catch (Exception ex) {

                        }
                    });

                    Thread.sleep(hearbeat_inteval);
                } catch (Exception ex) {

                }
            }
        }

    }


    /**
     * 数据线程
     */
    public class DataRequestThread implements Runnable {

        private MqMessage data;

        public DataRequestThread(MqMessage data) {
            this.data = data;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String msg = getMsgXml(data);
                    logger.debug("发送订阅请求::{}", msg);
                    executorService.submit(() -> {
                        try {
                            sendRequest(getHttpReq(msg));
                        } catch (Exception ex) {

                        }
                    });

                    Thread.sleep(datareq_inteval);
                } catch (Exception ex) {

                }
            }
        }

    }

    private Request getHttpReq(String msg) {
        RequestBody requestBody = RequestBody.create(mediaType, msg);
        return new Request.Builder().url(url).post(requestBody)
                .build();

    }

    private String getMsgXml(MqMessage mqMessage) throws Exception {
        mqMessage.getBody().setSeqNum(msgResolver.getUniqueSeq());
        mqMessage.getHeader().setToken(token);
        StringWriter writer = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(MqMessage.class,
                MqMessageHeader.class, MqMessageBody.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
        marshaller.marshal(mqMessage, writer);
        return writer.toString();
    }


    // 参数设置


    public String getSysCode() {
        return sysCode;
    }

    public void setSysCode(String sysCode) {
        this.sysCode = sysCode;
    }

    public String getDatatypes() {
        return datatypes;
    }

    public void setDatatypes(String datatypes) {
        this.datatypes = datatypes;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getHearbeat_inteval() {
        return hearbeat_inteval;
    }

    public void setHearbeat_inteval(long hearbeat_inteval) {
        this.hearbeat_inteval = hearbeat_inteval;
    }

    public long getDatareq_inteval() {
        return datareq_inteval;
    }

    public void setDatareq_inteval(long datareq_inteval) {
        this.datareq_inteval = datareq_inteval;
    }

    public IMsgResolver getMsgResolver() {
        return msgResolver;
    }

    public void setMsgResolver(IMsgResolver msgResolver) {
        this.msgResolver = msgResolver;
    }

    public void login() {
        if (StringUtils.isNotEmpty(token)) {
            return;
        }
        AuthMessage loginReq = new AuthMessage();
        AuthMessageHeader authMessageHeader = new AuthMessageHeader();
        AuthMessageBody authMessageBody = new AuthMessageBody();
        authMessageHeader.setReceiver(MqMessageConstant.Participate.DATACENTER.getParticipate());
        authMessageHeader.setSender(sysCode);
        authMessageHeader.setSendTime(new Date());
        authMessageBody.setUserName(username);
        authMessageBody.setPassWord(password);
        loginReq.setBody(authMessageBody);
        loginReq.setHeader(authMessageHeader);

        try {
            StringWriter writer = new StringWriter();
            JAXBContext jaxbContext = JAXBContext.newInstance(AuthMessage.class,
                    AuthMessageHeader.class, AuthMessageBody.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
            marshaller.marshal(loginReq, writer);
            logger.debug("发送请求::{}", writer.toString());
            RequestBody requestBody = RequestBody.create(mediaType, writer.toString());
            String loginurl = "";
            if (url.endsWith("/")) {
                loginurl = url + "login";
            } else {
                loginurl = url + "/login";
            }

            Request request = new Request.Builder()
                    .url(loginurl)
                    .post(requestBody)
                    .build();
            if (null != okHttpClient && null != request) {
                //执行请求，得到响应
                Response response = okHttpClient.newCall(request).execute();
                if (HttpServletResponse.SC_OK == response.code()) {
                    String resp = response.body().string();

                    if (StringUtils.isNotEmpty(resp)) {
                        AuthMessage authresp = (AuthMessage) unmarshaller.unmarshal(
                                new StreamSource(new ByteArrayInputStream(resp.getBytes())));
                        if ("000".equals(authresp.getBody().getCode())) {
                            token = authresp.getBody().getToken();
                            if (StringUtils.isEmpty(token)) {
                                logger.debug("登录失败");
                            } else {
                                logger.debug("登录成功，收到数据中心认证token::{}", token);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    /**
     * 发送订阅
     */
    public boolean subscribe() {
        if (StringUtils.isEmpty(token)) {
            return false;
        }
        MqMessage subs = new MqMessageBuilder()
                .msgType(MqMessageConstant.MsgType.SUBSCRIBE_REQUEST)
                .receiver(MqMessageConstant.Participate.DATACENTER.getParticipate())
                .sendTime(new Date())
                .sender(sysCode)
                .token(token)
                .dataType(this.datatypes)
                .build();

        try {
            StringWriter writer = new StringWriter();
            JAXBContext jaxbContext = JAXBContext.newInstance(MqMessage.class,
                    MqMessageHeader.class, MqMessageBody.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
            marshaller.marshal(subs, writer);
            logger.debug("发送订阅请求::{}", writer.toString());
            RequestBody requestBody = RequestBody.create(mediaType, writer.toString());
            String loginurl = url;
            Request request = new Request.Builder()
                    .url(loginurl)
                    .post(requestBody)
                    .build();

            if (null != okHttpClient && null != request) {
                //执行请求，得到响应
                Response response = okHttpClient.newCall(request).execute();
                if (HttpServletResponse.SC_OK == response.code()) {
                    String resp = response.body().string();

                    if (StringUtils.isNotEmpty(resp)) {
                        logger.debug("订阅请求成功，收到数据中心响应{}", resp);
                        MqMessage subsRsp = (MqMessage) unmarshaller.unmarshal(
                                new StreamSource(new ByteArrayInputStream(resp.getBytes())));
                        if (MqMessageConstant.MqMessageStatus.ACCEPT.getStatus().equals(subsRsp.getBody().getStatus())){
                            return true;
                        }

                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return false;
    }

    /**
     * 获取接口数据
     *
     * @param serviceCode
     * @param params
     * @return
     */
    public JSONObject getApiData(String serviceCode, JSONObject params) {
        JSONObject jsonObject = null;
        JSONObject reqbody = new JSONObject();
        reqbody.put("serviceCode", serviceCode);
        reqbody.put("token", token);
        reqbody.put("params", params);
        RequestBody requestBody = RequestBody.create(mediaTypeJSON, reqbody.toString());
        String apiUrl;
        if (url.endsWith("/")) {
            apiUrl = url + "api";
        } else {
            apiUrl = url + "/api";
        }

        Request request = new Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .build();
        if (null != okHttpClient && null != request) {
            //执行请求，得到响应
            try {
                Response response = okHttpClient.newCall(request).execute();
                if (HttpServletResponse.SC_OK == response.code()) {
                    String resp = response.body().string();

                    if (StringUtils.isNotEmpty(resp)) {
                        jsonObject = JSON.parseObject(resp);
                    }
                }
            } catch (Exception ex) {
                return null;
            }
        }
        return jsonObject;
    }
}
