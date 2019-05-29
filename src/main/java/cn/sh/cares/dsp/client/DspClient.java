package cn.sh.cares.dsp.client;

import cn.sh.cares.dsp.common.MqMessageConstant;
import cn.sh.cares.dsp.message.MqMessage;
import cn.sh.cares.dsp.message.MqMessageBody;
import cn.sh.cares.dsp.message.MqMessageBuilder;
import cn.sh.cares.dsp.message.MqMessageHeader;
import cn.sh.cares.dsp.message.auth.AuthMessage;
import cn.sh.cares.dsp.message.auth.AuthMessageBody;
import cn.sh.cares.dsp.message.auth.AuthMessageHeader;
import cn.sh.cares.dsp.utils.DspJson;
import cn.sh.cares.dsp.utils.HttpUtil;
import cn.sh.cares.dsp.utils.StringUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * 数据共享平台java 客户端
 */
public class DspClient {

    Logger logger = Logger.getLogger(getClass().getName());

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
     * 数据共享平台连接地址
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

    private boolean logEnabled = false;


    {
        AtomicLong atomicLong = new AtomicLong(1);
        executorService = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("DCS::CLIENT" + atomicLong.getAndIncrement());
                return thread;
            }
        });
    }

    private DspClient() {

    }

    private void checkParam() {
        if (null == msgResolver) {
            if (logEnabled)
                logger.severe("消息解析器不能为空");
            System.exit(-1);
        }

        if ("".equals(url) || null == url) {
            if (logEnabled)
                logger.severe("数据共享平台连接地址不能为空");
            System.exit(-1);
        }

        if ("".equals(sysCode) || null == sysCode) {
            if (logEnabled)
                logger.severe("系统代码不能为空");
            System.exit(-1);
        }

        if ("".equals(username) || null == username) {
            if (logEnabled)
                logger.severe("接入用户名不能为空");
            System.exit(-1);
        }

        if ("".equals(password) || null == password) {
            if (logEnabled)
                logger.severe("接入密码能为空");
            System.exit(-1);
        }

        if ("".equals(datatypes) || null == datatypes) {
            if (logEnabled)
                logger.severe("订阅数据不能为空");
            System.exit(-1);
        }
    }

    private static DspClient client = null;

    public static synchronized DspClient getClient() {
        if (null == client) {
            client = new DspClient();
        }
        return client;
    }

    public void start() {

        // 校验参数
        checkParam();

        // 登录数据共享平台
        login();

        // 发送订阅消息
        if (!subscribe()) {
            logger.severe("发送订阅失败");
            System.exit(-9);
        }

        if (StringUtil.isNotEmpty(token)) {
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
            logger.severe("登录数据共享平台失败，请检查");
            System.exit(-2);
        }
    }


    private void sendRequest(String request) throws Exception {
        String resp = HttpUtil.sendRequestXml(url, request);

        if (StringUtil.isNotEmpty(resp)) {
            JAXBContext jaxbContext = JAXBContext.newInstance(MqMessage.class,
                    MqMessageHeader.class, MqMessageBody.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            MqMessage mqMessage = (MqMessage) unmarshaller.unmarshal(
                    new StreamSource(new ByteArrayInputStream(resp.getBytes("UTF-8"))));
            if (null == mqMessage) {
                return;
            } else if (MqMessageConstant.MqMessageStatus.TOKENEXPIRE
                    .getStatus().equals(mqMessage.getBody().getStatus())) {
                synchronized (token) {
                    token = null;
                    if (logEnabled)
                        logger.severe("token 超时，重新登录");
                    login();
                }
            } else {
                if (MqMessageConstant.MsgType.DATA_RESPONES.equals(mqMessage.getHeader().getMsgType())) {

                    if (mqMessage.getBody().getList().getItem().size() > 0) {
                        if (logEnabled) {
                            logger.info("收到数据共享平台数据响应消息::" + resp);
                        }
                        msgResolver.resolve(resp);
                    }
                } else if (MqMessageConstant.MsgType.HEARTBEAT_RESPONES.equals(mqMessage.getHeader().getMsgType())) {
                    if (logEnabled)
                        logger.info("收到数据共享平台心跳响应消息::" + resp);

                } else if (MqMessageConstant.MsgType.SUBSCRIBE_RESPONES.equals(mqMessage.getHeader().getMsgType())) {
                    logger.info("收到数据共享平台订阅响应消息::" + resp);

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
                    if (logEnabled) logger.info("发送心跳请求::" + msg);
                    executorService.submit(() -> {
                        try {
                            sendRequest(msg);
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
                    if (logEnabled) logger.info("发送数据请求::" + msg);
                    executorService.submit(() -> {
                        try {
                            sendRequest(msg);
                        } catch (Exception ex) {

                        }
                    });

                    Thread.sleep(datareq_inteval);
                } catch (Exception ex) {

                }
            }
        }

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
        if (StringUtil.isNotEmpty(token)) {
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
            logger.info("发送登录请求::" + writer.toString());
            String loginurl = "";
            if (url.endsWith("/")) {
                loginurl = url + "login";
            } else {
                loginurl = url + "/login";
            }
            String resp = HttpUtil.sendRequestXml(loginurl, writer.toString());

            if (StringUtil.isNotEmpty(resp)) {
                AuthMessage authresp = (AuthMessage) unmarshaller.unmarshal(
                        new StreamSource(new ByteArrayInputStream(resp.getBytes("UTF-8"))));
                if ("000".equals(authresp.getBody().getCode())) {
                    token = authresp.getBody().getToken();
                    if (StringUtil.isEmpty(token)) {
                        logger.info("登录失败");
                    } else {
                        logger.info("登录成功，收到数据共享平台认证token::" + token);
                    }
                }
            } else {
                logger.info("登录失败");
                System.exit(-2);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * 发送订阅
     */
    public boolean subscribe() {
        if (StringUtil.isEmpty(token)) {
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
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
            marshaller.marshal(subs, writer);
            logger.info("发送订阅请求::" + writer.toString());
            sendRequest(writer.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    /**
     * 获取接口数据
     *
     * @param jsonbody
     * @return
     */
    public String getApiData(String jsonbody) {

        String apiUrl;
        if (url.endsWith("/")) {
            apiUrl = url + "api";
        } else {
            apiUrl = url + "/api";
        }

        DspJson reqbody = new DspJson();
        reqbody.put("token", token);

        return HttpUtil.sendRequestJson(apiUrl, reqbody.toString());
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

}