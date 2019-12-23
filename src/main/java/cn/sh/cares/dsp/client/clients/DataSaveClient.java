package cn.sh.cares.dsp.client.clients;

import cn.sh.cares.dsp.client.AbstractDspClient;
import cn.sh.cares.dsp.client.DspClientProperty;
import cn.sh.cares.dsp.common.MqMessageConstant;
import cn.sh.cares.dsp.message.Item;
import cn.sh.cares.dsp.message.List;
import cn.sh.cares.dsp.message.MqMessage;
import cn.sh.cares.dsp.message.MqMessageBuilder;
import cn.sh.cares.dsp.utils.HttpUtil;
import cn.sh.cares.dsp.utils.StringUtil;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.logging.Level;

/**
 * 数据接入客户端
 * @author wangcj
 */
public class DataSaveClient extends AbstractDspClient {


    public DataSaveClient(DspClientProperty dspClientProperty) {
        super(dspClientProperty);
        DSP_CLIENT_URL = dspClientProperty.getUrl();
        if (dspClientProperty.getUrl().endsWith(URL_SEP_CHAR)) {
            DSP_CLIENT_DATASAVE_URL = dspClientProperty.getUrl() + DSP_DATAIN_URL;
        } else {
            DSP_CLIENT_DATASAVE_URL = dspClientProperty.getUrl() + URL_SEP_CHAR+ DSP_DATAIN_URL;
        }
        classes.addAll(dspClientProperty.getClassesInput());
        try {
            jaxbContext = JAXBContext.newInstance(classes.toArray(new Class[classes.size()]));
            marshaller = jaxbContext.createMarshaller();
            unmarshaller = jaxbContext.createUnmarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8.name());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void checkParam() {
        super.checkParam();
        if (StringUtil.isEmpty(dspClientProperty.getDatatypes())) {
            logger.severe("接入服务不能为空");
            System.exit(-1);
        }

        if (dspClientProperty.getClassesInput() == null || dspClientProperty.getClassesInput().size() == 0) {
            logger.severe("接入数据类型不能为空");
            System.exit(-1);
        }
    }

    @Override
    protected void login() {
        super.login();
        loginReq.getHeader().setAuthType("DATAIN");
        doLogin();
    }

    @Override
    public void start() {
        super.start();
        heartMsg = new MqMessageBuilder()
                .msgType(MqMessageConstant.MsgType.DATAIN_HEARTBEAT_REQUEST)
                .receiver(MqMessageConstant.Participate.DATACENTER.getParticipateName())
                .sendTime(new Date())
                .sender(dspClientProperty.getSysCode())
                .token(dspClientProperty.getToken())
                .build();
        executorService.submit(new HeartBeatTimer());
    }

    public  void putData(java.util.List<Item> itemList) {

        createContext();
        MqMessage mqMessage = new MqMessageBuilder()
                .msgType(MqMessageConstant.MsgType.DATA_ARRIVAL_REQUEST)
                .receiver(MqMessageConstant.Participate.DATACENTER.getParticipateName())
                .sendTime(new Date())
                .sender(dspClientProperty.getSysCode())
                .token(dspClientProperty.getToken())
                .dataType(dspClientProperty.getDatatypes())
                .build();
        List list = new List();
        list.setItem(itemList);
        mqMessage.getBody().setList(list);
        String req = toXml(mqMessage,marshaller);
        if (dspClientProperty.isLogEnabled()) {
            logger.log(Level.INFO,"接入数据存储请求{0}",req);
        }
        String resp = HttpUtil.putXml(DSP_CLIENT_DATASAVE_URL, req);
        processReturnMsg(resp);
    }
}
