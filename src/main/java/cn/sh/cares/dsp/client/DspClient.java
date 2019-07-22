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
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 数据共享平台java 客户端
 *
 * @author wangcj
 */
public class DspClient {

    private Logger logger = Logger.getLogger(getClass().getName());

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
     * 默认心跳间隔
     */
    private Long hearbeatInteval = 60000L;
    /**
     * 最小心跳间隔
     */
    private static Long hearbeatIntevalMin = 60000L;
    /**
     * 最大心跳间隔
     */
    private static Long hearbeatIntevalMax = 600000L;

    /**
     * 数据请求间隔
     */
    private Long datareqInteval = 5000L;

    private IMsgResolver msgResolver;

    private ExecutorService executorService;

    private boolean logEnabled = false;

    private boolean subscribed = false;

    private static final String URL_SEP_CHAR = "/";
    private static final String LOGIN_SUCCESS_CODE = "000";

    private DspClient() {
        AtomicLong atomicLong = new AtomicLong(1);
        BlockingQueue<Runnable> blockingQueue = new LinkedBlockingDeque<>(100);
        executorService = new ThreadPoolExecutor(10, 100, 60,
                TimeUnit.MINUTES, blockingQueue, r -> {
            Thread thread = new Thread(r);
            thread.setName("DSP::CLIENT" + atomicLong.getAndIncrement());
            return thread;
        });

        // 注册关闭服务钩子
        Runtime.getRuntime().addShutdownHook(new Thread(new SendUnsubscribe()));
    }

    /**
     * 检查客户端参数
     */
    private void checkParam() {
        if (null == msgResolver) {
            logger.severe("消息解析器不能为空");
            System.exit(-1);
        }

        if (StringUtil.isEmpty(url)) {
            logger.severe("数据共享平台连接地址不能为空");
            System.exit(-1);
        }

        if (StringUtil.isEmpty(sysCode)) {
            logger.severe("系统代码不能为空");
            System.exit(-1);
        }

        if (StringUtil.isEmpty(username)) {
            logger.severe("接入用户名不能为空");
            System.exit(-1);
        }

        if (StringUtil.isEmpty(password)) {
            logger.severe("接入密码不能为空");
            System.exit(-1);
        }

        if (StringUtil.isEmpty(datatypes)) {
            logger.severe("订阅数据不能为空");
            System.exit(-1);
        }

        if (hearbeatInteval < hearbeatIntevalMin || hearbeatInteval > hearbeatIntevalMax) {
            hearbeatInteval = 60000L;
        }

        if (datareqInteval > 5000L || datareqInteval < 1000L) {
            datareqInteval = 1000L;
        }
    }

    private static DspClient client = null;


    public static synchronized DspClient getClient() {
        if (null == client) {
            client = new DspClient();
        }
        return client;
    }

    /**
     * 启动客户端
     */
    public void start() {

        // 校验参数
        checkParam();

        // 登录数据共享平台
        login();

        // 发送订阅消息
        subscribe();


        if (StringUtil.isNotEmpty(token)) {
            executorService.submit(new HeartBeatRequestThread(new MqMessageBuilder()
                    .msgType(MqMessageConstant.MsgType.HEARTBEAT_REQUEST)
                    .receiver(MqMessageConstant.Participate.DATACENTER.getParticipateName())
                    .sendTime(new Date())
                    .sender(sysCode)
                    .token(token)
                    .build()));

            executorService.submit(new DataRequestThread(new MqMessageBuilder()
                    .msgType(MqMessageConstant.MsgType.DATA_REQUEST)
                    .receiver(MqMessageConstant.Participate.DATACENTER.getParticipateName())
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


    private void sendRequest(String request) throws JAXBException {
        String resp = HttpUtil.sendRequestXml(url, request);

        if (StringUtil.isEmpty(resp)) {
            return;
        }

        JAXBContext jaxbContext = JAXBContext.newInstance(MqMessage.class,
                MqMessageHeader.class, MqMessageBody.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        MqMessage mqMessage = (MqMessage) unmarshaller.unmarshal(
                new StreamSource(new ByteArrayInputStream(resp.getBytes(StandardCharsets.UTF_8))));

        if (MqMessageConstant.MqMessageStatus.TOKENEXPIRE
                .getStatus().equals(mqMessage.getBody().getStatus())) {
            synchronized (DspClient.class) {
                token = null;
                if (logEnabled) {
                    logger.severe("token 超时，重新登录");
                }
                login();
                subscribe();
            }

            return;
        }

        switch (mqMessage.getHeader().getMsgType()) {
            case MqMessageConstant.MsgType.DATA_RESPONES:

                if (Optional.ofNullable(mqMessage.getBody().getList().getItem()).isPresent()) {
                    if (logEnabled) {
                        logger.log(Level.INFO, "收到数据共享平台数据响应消息::{0}", resp);
                    }
                    // 防止解析线程发生异常
                    executorService.submit(() -> msgResolver.resolve(resp));
                    synchronized (DspClient.class) {
                        this.datareqInteval = 100L;
                    }

                } else {
                    synchronized (DspClient.class) {
                        this.datareqInteval = 1000L;
                    }
                }
                break;
            case MqMessageConstant.MsgType.NO_DATA_RESPONSE:
                synchronized (DspClient.class) {
                    this.datareqInteval = 1000L;
                }
                break;
            case MqMessageConstant.MsgType.HEARTBEAT_RESPONES:
                if (logEnabled) {
                    logger.log(Level.INFO, "收到数据共享平台心跳响应消息::{0}", resp);
                }
                break;
            case MqMessageConstant.MsgType.SUBSCRIBE_RESPONES:
                logger.log(Level.INFO, "收到数据共享平台订阅响应消息::{0}", resp);
                subscribed = true;
                break;
            case MqMessageConstant.MsgType.SUBSCRIBE_C_RESPONES:
                logger.log(Level.INFO, "收到数据共享平台取消订阅响应消息::{0}", resp);
                break;
            default:
                break;
        }
    }


    /**
     * 心跳线程
     */
    public class HeartBeatRequestThread implements Runnable {
        private MqMessage heart;

        private HeartBeatRequestThread(MqMessage heart) {
            this.heart = heart;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String msg = getMsgXml(heart);
                    if (logEnabled) {
                        logger.log(Level.INFO, "发送心跳请求::{0}", msg);
                    }
                    sendRequest(msg);
                    Thread.sleep(hearbeatInteval);
                } catch (Exception ex) {
                    logger.severe("心跳线程异常");
                }
            }
        }

    }


    /**
     * 数据线程
     */
    public class DataRequestThread implements Runnable {

        private MqMessage data;

        private DataRequestThread(MqMessage data) {
            this.data = data;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String msg = getMsgXml(data);
                    if (logEnabled) {
                        logger.log(Level.INFO, "发送数据请求::{0}", msg);
                    }
                    sendRequest(msg);
                    Thread.sleep(datareqInteval);
                } catch (Exception ex) {

                    logger.severe("数据线程异常");

                    if (ex instanceof SocketTimeoutException
                            || ex instanceof SocketException
                            || ex instanceof ConnectException) {
                        synchronized (DspClient.class) {
                            datareqInteval = 180000L;
                        }
                    }

                    logger.log(Level.SEVERE, "数据线程异常", ex);
                }
            }
        }

    }


    private String getMsgXml(MqMessage mqMessage) throws JAXBException {
        mqMessage.getBody().setSeqNum(msgResolver.getUniqueSeq());
        mqMessage.getHeader().setToken(token);
        StringWriter writer = new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(MqMessage.class,
                MqMessageHeader.class, MqMessageBody.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());

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

    public Long getHearbeatInteval() {
        return hearbeatInteval;
    }

    public void setHearbeatInteval(Long hearbeatInteval) {
        this.hearbeatInteval = hearbeatInteval;
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

    public IMsgResolver getMsgResolver() {
        return msgResolver;
    }

    public void setMsgResolver(IMsgResolver msgResolver) {
        this.msgResolver = msgResolver;
    }

    public void setDatatypes(String datatypes) {
        this.datatypes = datatypes;
    }

    public Long getDatareqInteval() {
        return datareqInteval;
    }

    public void setDatareqInteval(Long datareqInteval) {
        this.datareqInteval = datareqInteval;
    }

    /**
     * 登录
     */
    public void login() {
        if (StringUtil.isNotEmpty(token)) {
            return;
        }

        AuthMessage loginReq = new AuthMessage();
        AuthMessageHeader authMessageHeader = new AuthMessageHeader();
        AuthMessageBody authMessageBody = new AuthMessageBody();
        authMessageHeader.setReceiver(MqMessageConstant.Participate.DATACENTER.getParticipateName());
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
            marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
            marshaller.marshal(loginReq, writer);
            logger.log(Level.INFO, "发送登录请求::{0}", writer);
            String loginurl;
            if (url.endsWith(URL_SEP_CHAR)) {
                loginurl = url + "login";
            } else {
                loginurl = url + "/login";
            }
            String resp = HttpUtil.sendRequestXml(loginurl, writer.toString());

            if (StringUtil.isNotEmpty(resp)) {
                AuthMessage authresp = (AuthMessage) unmarshaller.unmarshal(
                        new StreamSource(new ByteArrayInputStream(resp.getBytes(StandardCharsets.UTF_8))));
                if (LOGIN_SUCCESS_CODE.equals(authresp.getBody().getCode())) {
                    token = authresp.getBody().getToken();
                    if (StringUtil.isEmpty(token)) {
                        logger.info("登录失败");
                    } else {
                        logger.log(Level.INFO, "登录成功，收到数据共享平台认证token::{0}", token);
                    }
                }
            } else {
                logger.info("登录失败");
                System.exit(-2);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "登录出错", ex);
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
                .receiver(MqMessageConstant.Participate.DATACENTER.getParticipateName())
                .sendTime(new Date())
                .sender(sysCode)
                .token(token)
                .dataType(datatypes)
                .build();

        try {
            String xml = genXml(subs);
            logger.log(Level.INFO, "发送订阅请求::{0}", xml);
            sendRequest(xml);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "发送订阅请求出错", ex);
        }

        return true;
    }

    /**
     * 发送取消订阅
     *
     * @return 发送是否成功
     */
    public boolean unsubscribe() {
        if (StringUtil.isEmpty(token)) {
            return false;
        }
        MqMessage subs = new MqMessageBuilder()
                .msgType(MqMessageConstant.MsgType.SUBSCRIBE_C_REQUEST)
                .receiver(MqMessageConstant.Participate.DATACENTER.getParticipateName())
                .sendTime(new Date())
                .sender(sysCode)
                .token(token)
                .dataType(this.datatypes)
                .build();

        try {
            String xml = genXml(subs);
            logger.log(Level.INFO, "发送取消订阅::{0}", xml);
            sendRequest(xml);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "发送取消订阅出错", ex);
            return false;
        }

        return true;
    }

    private String genXml(MqMessage mqMessage) {
        JAXBContext jaxbContext;
        StringWriter writer = new StringWriter();
        try {
            jaxbContext = JAXBContext.newInstance(MqMessage.class,
                    MqMessageHeader.class, MqMessageBody.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
            marshaller.marshal(mqMessage, writer);
        } catch (JAXBException e) {
            logger.log(Level.SEVERE, "序列化消息出错", e);
        }
        return writer.toString();

    }

    /**
     * 获取接口数据
     *
     * @param jsonbody 请求参数
     * @return json格式字符串
     */
    public String getApiData(String jsonbody) {

        String apiUrl;
        if (url.endsWith(URL_SEP_CHAR)) {
            apiUrl = url + "api";
        } else {
            apiUrl = url + "/api";
        }

        DspJson reqbody = new DspJson();
        reqbody.parse(jsonbody);
        reqbody.put("token", token);
        return HttpUtil.sendRequestJson(apiUrl, reqbody.toString());
    }

    public boolean isLogEnabled() {
        return logEnabled;
    }

    public void setLogEnabled(boolean logEnabled) {
        this.logEnabled = logEnabled;
    }

    public class SendUnsubscribe implements Runnable {

        @Override
        public void run() {
            if (subscribed) {
                unsubscribe();
                logger.info("发送取消订阅成功");
            }
        }
    }
}
