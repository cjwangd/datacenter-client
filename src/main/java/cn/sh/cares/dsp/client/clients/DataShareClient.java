package cn.sh.cares.dsp.client.clients;

import cn.sh.cares.dsp.client.AbstractDspClient;
import cn.sh.cares.dsp.client.DspClientProperty;
import cn.sh.cares.dsp.common.MqMessageConstant;
import cn.sh.cares.dsp.message.MqMessage;
import cn.sh.cares.dsp.message.MqMessageBuilder;
import cn.sh.cares.dsp.utils.FileUtils;
import cn.sh.cares.dsp.utils.HttpUtil;
import cn.sh.cares.dsp.utils.StringUtil;

import java.util.Date;
import java.util.logging.Level;
/**
 * 数据共享客户端
 * @author wangcj
 */
public class DataShareClient extends AbstractDspClient {
    public DataShareClient(DspClientProperty dspClientProperty) {
        super(dspClientProperty);
        DSP_CLIENT_URL = dspClientProperty.getUrl();
    }


    @Override
    protected void checkParam() {
        super.checkParam();
        super.checkParam();
        if (StringUtil.isEmpty(dspClientProperty.getDatatypes())) {
            logger.severe("订阅数据不能为空");
            System.exit(-1);
        }

        if (dspClientProperty.getHearbeatInteval() < hearbeatIntevalMin
                || dspClientProperty.getHearbeatInteval() > hearbeatIntevalMax) {
            dspClientProperty.setHearbeatInteval(60000L);
        }

        if (dspClientProperty.getDatareqInteval() > dataIntervalMax
                || dspClientProperty.getDatareqInteval() < dataIntervalMin) {
            dspClientProperty.setDatareqInteval(1000L);
        }
    }

    @Override
    public void login() {
        super.login();
        loginReq.getHeader().setAuthType("DATASHARE");
        doLogin();
    }

    @Override
    public void start() {
        super.start();

      //  unsubscribe();
        subscribe();
        heartMsg = new MqMessageBuilder()
                .msgType(MqMessageConstant.MsgType.HEARTBEAT_REQUEST)
                .receiver(MqMessageConstant.Participate.DATACENTER.getParticipateName())
                .sendTime(new Date())
                .sender(dspClientProperty.getSysCode())
                .token(dspClientProperty.getToken())
                .build();
        dataMsg = new MqMessageBuilder()
                .msgType(MqMessageConstant.MsgType.DATA_REQUEST)
                .receiver(MqMessageConstant.Participate.DATACENTER.getParticipateName())
                .sendTime(new Date())
                .sender(dspClientProperty.getSysCode())
                .token(dspClientProperty.getToken())
                .dataType(dspClientProperty.getDatatypes())
                .build();

        executorService.submit(new HeartBeatTimer());
        executorService.submit(new DataTimer());

    }

    /**
     * 发送订阅
     */
    public boolean subscribe() {
        if (StringUtil.isEmpty(dspClientProperty.getToken())) {
            return false;
        }
        MqMessage subs = new MqMessageBuilder()
                .msgType(MqMessageConstant.MsgType.SUBSCRIBE_REQUEST)
                .receiver(MqMessageConstant.Participate.DATACENTER.getParticipateName())
                .sendTime(new Date())
                .sender(dspClientProperty.getSysCode())
                .token(dspClientProperty.getToken())
                .dataType(dspClientProperty.getDatatypes())
                .build();

        try {
            String resp = HttpUtil.sendRequestXml(DSP_CLIENT_URL, toXml(subs,marshaller));
            processReturnMsg(resp);
            logger.log(Level.INFO, "发送订阅请求::{0}", resp);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, "发送订阅请求出错", ex);
        }

        FileUtils.saveSubscribe(dspClientProperty.getDatatypes());

        return true;
    }

    /**
     * 发送取消订阅
     *
     * @return 发送是否成功
     */
    private boolean unsubscribe() {
        if (StringUtil.isEmpty(dspClientProperty.getToken())) {
            return false;
        }

        String dataTypes = FileUtils.readSubscribe();
        if (null == dataTypes) {
            return true;
        }

        MqMessage subs = new MqMessageBuilder()
                .msgType(MqMessageConstant.MsgType.SUBSCRIBE_C_REQUEST)
                .receiver(MqMessageConstant.Participate.DATACENTER.getParticipateName())
                .sendTime(new Date())
                .sender(dspClientProperty.getSysCode())
                .token(dspClientProperty.getToken())
                .dataType(dataTypes)
                .build();

        try {

            String resp = HttpUtil.sendRequestXml(DSP_CLIENT_URL, toXml(subs,marshaller));
            processReturnMsg(resp);
            logger.log(Level.INFO, "发送取消订阅::{0}", resp);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "发送取消订阅出错", ex);
            return false;
        }

        return true;
    }

    /**
     * 发送取消订阅
     *
     * @param dataTypes 要取消订阅的消息类型逗号分隔
     *
     * @return 发送是否成功
     */
    public boolean unsubscribe(String dataTypes) {
        if (StringUtil.isEmpty(dspClientProperty.getToken())) {
            return false;
        }
        MqMessage subs = new MqMessageBuilder()
                .msgType(MqMessageConstant.MsgType.SUBSCRIBE_C_REQUEST)
                .receiver(MqMessageConstant.Participate.DATACENTER.getParticipateName())
                .sendTime(new Date())
                .sender(dspClientProperty.getSysCode())
                .token(dspClientProperty.getToken())
                .dataType(dataTypes)
                .build();

        try {
            String resp = HttpUtil.sendRequestXml(DSP_CLIENT_URL, toXml(subs,marshaller));
            processReturnMsg(resp);
            logger.log(Level.INFO, "发送取消订阅::{0}", resp);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "发送取消订阅出错", ex);
            return false;
        }

        return true;
    }
}
