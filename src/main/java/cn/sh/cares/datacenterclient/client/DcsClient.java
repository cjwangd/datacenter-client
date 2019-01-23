package cn.sh.cares.datacenterclient.client;

import cn.sh.cares.datacenterclient.common.MqMessageConstant;
import cn.sh.cares.datacenterclient.message.MqMessage;
import cn.sh.cares.datacenterclient.message.MqMessageBody;
import cn.sh.cares.datacenterclient.message.MqMessageBuilder;
import cn.sh.cares.datacenterclient.message.MqMessageHeader;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 数据中心java 客户端
 */
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
    /**
     * 系统token
     **/
    private String token;
    /**
     * 数据中心连接地址
     **/
    private String url;
    /**
     * 每秒发送请求数
     **/
    private int freq = 1;
    private IMsgResolver msgResolver;
    private AtomicLong atomicLong = new AtomicLong(1);
    private Logger logger = LoggerFactory.getLogger(DcsClient.class);

    @Override
    public void afterPropertiesSet() {

        Assert.notNull(msgResolver, "消息解析器不能为空");
        Assert.notNull(url, "数据中心连接地址不能为空");
        Assert.notNull(sysCode, "系统代码不能为空");

        ExecutorService executorService = new ThreadPoolExecutor(10, 100, 7, TimeUnit.DAYS, new ArrayBlockingQueue<>(100));
        for (int i = 0; i < freq; i++) {
            executorService.submit(new HearBeatThread());
            datatypes.stream().forEach(d -> {
                executorService.submit(new DataRequestThread(d));
            });
        }
    }


    public void setSysCode(String sysCode) {
        this.sysCode = sysCode;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setFreq(int freq) {
        this.freq = freq;
    }

    public void setMsgResolver(IMsgResolver msgResolver) {
        this.msgResolver = msgResolver;
    }

    public void setDatatypes(List<String> datatypes) {
        this.datatypes = datatypes;
    }

    private void sendRequest(Request request) throws IOException {
        if (null != okHttpClient && null != request) {
            //执行请求，得到响应
            Response response = okHttpClient.newCall(request).execute();
            if (HttpServletResponse.SC_OK == response.code()) {
                String resp = response.body().string();
                logger.debug("收到数据中心响应消息::{}", resp);
                if (StringUtils.isNotEmpty(resp)) {
                    msgResolver.resolve(resp);
                }
            }
        }
    }

    /**
     * 数据订阅线程
     */
    public class DataRequestThread implements Runnable {

        private MqMessage data;
        private String datatype;

        DataRequestThread(String datatype) {
            this.datatype = datatype;
            data = new MqMessageBuilder()
                    .msgType(MqMessageConstant.MsgType.SUBSCRIBE_REQUEST)
                    .receiver(MqMessageConstant.Participate.DATACENTER.getParticipate())
                    .sendTime(new Date())
                    .sender(sysCode)
                    .token(token)
                    .build();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    JAXBContext context = JAXBContext.newInstance(MqMessage.class, MqMessageHeader.class, MqMessageBody.class);
                    Marshaller marshaller = context.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");
                    data.getBody().setSeqNum(new Long(atomicLong.addAndGet(1L)).toString());
                    data.getBody().setDataType(datatype);
                    StringWriter writer = new StringWriter();
                    marshaller.marshal(data, writer);
                    logger.debug("发送订阅请求::{}", writer.toString());
                    RequestBody requestBody = RequestBody.create(mediaType, writer.toString());
                    Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();
                    sendRequest(request);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("发送请求数据异常", e);
                }
            }
        }


    }

    /**
     * 心跳线程
     */
    public class HearBeatThread implements Runnable {

        private MqMessage heart;

        HearBeatThread() {
            heart = new MqMessageBuilder()
                    .msgType(MqMessageConstant.MsgType.HEARTBEAT_REQUEST)
                    .receiver(MqMessageConstant.Participate.DATACENTER.getParticipate())
                    .sendTime(new Date())
                    .sender(sysCode)
                    .token(token)
                    .build();
        }

        @Override
        public void run() {
            while (true) {
                try {
                    JAXBContext context = JAXBContext.newInstance(MqMessage.class, MqMessageHeader.class, MqMessageBody.class);
                    Marshaller marshaller = context.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_ENCODING, "utf-8");

                    heart.getBody().setSeqNum(new Long(atomicLong.addAndGet(1L)).toString());
                    StringWriter writer = new StringWriter();
                    marshaller.marshal(heart, writer);
                    logger.debug("发送心跳请求::{}", writer.toString());
                    RequestBody requestBody = RequestBody.create(mediaType, writer.toString());
                    Request request = new Request.Builder()
                            .url(url)
                            .post(requestBody)
                            .build();
                    sendRequest(request);
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("发送请求数据异常", e);
                }
            }
        }
    }
}
