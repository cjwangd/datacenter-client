package cn.sh.cares.dsp.client;

import cn.sh.cares.dsp.common.MqMessageConstant;
import cn.sh.cares.dsp.message.*;
import cn.sh.cares.dsp.message.auth.AuthMessage;
import cn.sh.cares.dsp.message.auth.AuthMessageBody;
import cn.sh.cares.dsp.message.auth.AuthMessageBuilder;
import cn.sh.cares.dsp.message.auth.AuthMessageHeader;
import cn.sh.cares.dsp.utils.HttpUtil;
import cn.sh.cares.dsp.utils.StringUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
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
public abstract class AbstractDspClient {

    protected DspClientProperty dspClientProperty;
    protected Logger logger = Logger.getLogger(getClass().getName());
    protected JAXBContext jaxbContext = null;
    protected JAXBContext jaxbContextAuth = null;
    protected List<Class> classes = new ArrayList<>();
    protected Marshaller marshaller = null;
    protected Marshaller marshallerAuth = null;
    protected Unmarshaller unmarshaller = null;
    protected Unmarshaller unmarshallerAuth = null;

    protected static final String URL_SEP_CHAR = "/";
    protected static final String LOGIN_SUCCESS_CODE = "000";
    protected static final String DSP_LOGIN_URL = "login";
    protected static final String DSP_API_URL = "api";
    protected static final String DSP_DATAIN_URL = "putData";

    /**
     * 最小心跳间隔
     */
    protected static Long hearbeatIntevalMin = 60000L;
    /**
     * 最大心跳间隔
     */
    protected static Long hearbeatIntevalMax = 600000L;

    protected static Long dataIntervalMax = 5000L;
    protected static Long dataIntervalMin = 100L;

    protected  String DSP_CLIENT_URL = "";
    protected  String DSP_CLIENT_LOGIN_URL = "";
    protected  String DSP_CLIENT_DATASAVE_URL = "";

    protected AuthMessage loginReq;
    protected MqMessage heartMsg;
    protected MqMessage dataMsg;

    private static AtomicLong atomicLong = new AtomicLong(1);

    private static SynchronousQueue<Runnable> blockingQueue = new SynchronousQueue<Runnable>();
    protected static ExecutorService executorService = new ThreadPoolExecutor(
            Runtime.getRuntime().availableProcessors(), 100, 60,
            TimeUnit.MINUTES, blockingQueue, r -> {
        Thread thread = new Thread(r);
        thread.setName("DSP::CLIENT" + atomicLong.getAndIncrement());
        return thread;
    });


    public AbstractDspClient(DspClientProperty dspClientProperty) {
        this.dspClientProperty = dspClientProperty;

        createContext();

        classes.add(MqMessage.class);
        classes.add(MqMessageBody.class);
        classes.add(MqMessageHeader.class);
        classes.add(Data.class);
        classes.add(Item.class);
        classes.add(cn.sh.cares.dsp.message.List.class);
        try {
            jaxbContext = JAXBContext.newInstance(classes.toArray(new Class[classes.size()]));
            jaxbContextAuth = JAXBContext.newInstance(AuthMessage.class, AuthMessageHeader.class, AuthMessageBody.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        if (dspClientProperty.getUrl().endsWith(URL_SEP_CHAR)) {
            DSP_CLIENT_LOGIN_URL = dspClientProperty.getUrl() + DSP_LOGIN_URL;
        } else {
            DSP_CLIENT_LOGIN_URL = dspClientProperty.getUrl() + URL_SEP_CHAR + DSP_LOGIN_URL;
        }

        loginReq = new AuthMessageBuilder()
                .username(dspClientProperty.getUsername())
                .password(dspClientProperty.getPassword())
                .sender(dspClientProperty.getSysCode())
                .build();
    }


    /**
     * 检查客户端参数
     */
    protected void checkParam() {
        if (StringUtil.isEmpty(dspClientProperty.getUrl())) {
            logger.severe("数据共享平台连接地址不能为空");
            System.exit(-1);
        }

        if (StringUtil.isEmpty(dspClientProperty.getSysCode())) {
            logger.severe("系统代码不能为空");
            System.exit(-1);
        }

        if (StringUtil.isEmpty(dspClientProperty.getUsername())) {
            logger.severe("接入用户名不能为空");
            System.exit(-1);
        }

        if (StringUtil.isEmpty(dspClientProperty.getPassword())) {
            logger.severe("接入密码不能为空");
            System.exit(-1);
        }
    }

    /**
     * 登录
     */
    protected void login() {
        if (StringUtil.isNotEmpty(dspClientProperty.getToken())) {
            return;
        }
    }


    /**
     * 启动客户端
     */
    public void start() {
        checkParam();
        login();
    }

    public synchronized Object fromXml(String xml, Unmarshaller unmarshaller) {
        Object o = null;
        try {
            o = unmarshaller.unmarshal(
                    new StreamSource(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
        return o;
    }

    public synchronized String toXml(Object o, Marshaller marshaller) {

        String ret = null;
        StringWriter writer = new StringWriter();
        try {
            marshaller.marshal(o, writer);
            ret = writer.toString();
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
        return ret;
    }


    protected void processReturnMsg(String resp) {

        boolean logEnabled = dspClientProperty.isLogEnabled();
        IMsgResolver msgResolver = dspClientProperty.getMsgResolver();
        MqMessage mqMessage = null;
        if (resp == null || "".equals(resp)) {
            return;
        }
        mqMessage = (MqMessage) fromXml(resp, unmarshaller);

        if (null == mqMessage) {
            return;
        }
        if (MqMessageConstant.MqMessageStatus.TOKENEXPIRE
                .getStatus().equals(mqMessage.getBody().getStatus())) {
            synchronized (AbstractDspClient.class) {
                dspClientProperty.setToken(null);
                if (logEnabled) {
                    logger.severe("token 超时，重新登录");
                }
                login();
            }

            return;
        }

        String msgtype = mqMessage.getHeader().getMsgType();
        if ("".equals(msgtype) || null == msgtype) {
            return;
        }

        switch (msgtype) {
            case MqMessageConstant.MsgType.DATA_RESPONES:

                if (Optional.ofNullable(mqMessage.getBody().getList().getItem()).isPresent()) {
                    if (logEnabled) {
                        logger.log(Level.INFO, "收到数据共享平台数据响应消息::{0}", resp);
                    }
                    // 防止解析线程发生异常
                    executorService.submit(() -> msgResolver.resolve(resp));
                }
                break;
            case MqMessageConstant.MsgType.NO_DATA_RESPONSE:
                break;
            case MqMessageConstant.MsgType.HEARTBEAT_RESPONES:
                if (logEnabled) {
                    logger.log(Level.INFO, "收到数据共享平台心跳响应消息::{0}", resp);
                }
                break;
            case MqMessageConstant.MsgType.DATAIN_HEARTBEAT_RESPONSE:
                if (logEnabled) {
                    logger.log(Level.INFO, "收到数据共享平台数据接入心跳响应消息::{0}", resp);
                }
                break;

            case MqMessageConstant.MsgType.DATA_ARRIVAL_RESPONSE:
                if (logEnabled) {
                    logger.log(Level.INFO, "收到数据共享平台数据接入响应消息::{0}", resp);
                }
                executorService.submit(() -> msgResolver.resolve(resp));
                break;
            case MqMessageConstant.MsgType.SUBSCRIBE_RESPONES:
                if (logEnabled) {
                    logger.log(Level.INFO, "收到数据共享平台订阅响应消息::{0}", resp);
                }
                break;
            case MqMessageConstant.MsgType.SUBSCRIBE_C_RESPONES:
                if (logEnabled) {
                    logger.log(Level.INFO, "收到数据共享平台取消订阅响应消息::{0}", resp);
                }
                break;
            default:
                break;
        }
    }

    protected void doLogin() {
        try {
            createContext();

            String loginxml = toXml(loginReq, marshallerAuth);
            logger.log(Level.INFO, "发送登录请求::{0}", loginxml);
            String resp = HttpUtil.sendRequestXml(DSP_CLIENT_LOGIN_URL, loginxml);

            if (StringUtil.isNotEmpty(resp)) {
                AuthMessage authresp = (AuthMessage) fromXml(resp, unmarshallerAuth);
                if (LOGIN_SUCCESS_CODE.equals(authresp.getBody().getCode())) {
                    String token = authresp.getBody().getToken();
                    if (StringUtil.isEmpty(token)) {
                        logger.info("登录失败");
                    } else {
                        dspClientProperty.setToken(token);
                        logger.log(Level.INFO, "登录成功，收到数据共享平台认证token::{0}", token);
                    }
                } else {
                    logger.info("登录失败");
                    System.exit(-2);
                }

            } else {
                logger.info("登录失败");
                System.exit(-2);
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "登录出错", ex);
        }
    }


    protected void createContext() {
        try {
            marshaller = jaxbContext.createMarshaller();
            marshallerAuth = jaxbContextAuth.createMarshaller();
            unmarshaller = jaxbContext.createUnmarshaller();
            unmarshallerAuth = jaxbContextAuth.createUnmarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
            marshallerAuth.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
        } catch (Exception e) {

        }

    }

    public class HeartBeatTimer implements Runnable {

        @Override
        public void run() {
            executorService.submit(() -> {
                while (true) {
                    createContext();
                    heartMsg.getHeader().setToken(dspClientProperty.getToken());
                    try {
                        String resp = HttpUtil.sendRequestXml(DSP_CLIENT_URL, toXml(heartMsg, marshaller));
                        processReturnMsg(resp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Thread.sleep(dspClientProperty.getHearbeatInteval());
                }
            });

        }
    }

    public class DataTimer implements Runnable {

        @Override
        public void run() {
            executorService.submit(() -> {
                while (true) {
                    createContext();
                    dataMsg.getHeader().setToken(dspClientProperty.getToken());
                    try {
                        String resp = HttpUtil.sendRequestXml(DSP_CLIENT_URL, toXml(dataMsg, marshaller));
                        processReturnMsg(resp);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Thread.sleep(dspClientProperty.getDatareqInteval());
                }
            });

        }
    }


}


