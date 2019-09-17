package cn.sh.cares.dsp.client;

import java.util.List;

/**
 * 客户端构建器
 * @author wangcj
 */
public class DspClientFactoryBuilder {

    private DspClientProperty dspClientProperty;
    public DspClientFactoryBuilder() {
        dspClientProperty = new DspClientProperty();
    }

    public DspClientFactoryBuilder url(String url) {
        dspClientProperty.setUrl(url);
        return this;
    }

    public DspClientFactoryBuilder username(String username) {
        dspClientProperty.setUsername(username);
        return this;
    }

    public DspClientFactoryBuilder password(String password) {
        dspClientProperty.setPassword(password);
        return this;
    }

    public DspClientFactoryBuilder syscode(String syscode) {
        dspClientProperty.setSysCode(syscode);
        return this;
    }

    public  DspClientFactoryBuilder msgResolver(IMsgResolver iMsgResolver) {
        dspClientProperty.setMsgResolver(iMsgResolver);
        return this;
    }

    public  DspClientFactoryBuilder heartBeatInterval(Long heartBeatInterval) {
        dspClientProperty.setHearbeatInteval(heartBeatInterval);
        return this;
    }

    public  DspClientFactoryBuilder dataInterval(Long dataInteval) {
        dspClientProperty.setDatareqInteval(dataInteval);
        return this;
    }

    public DspClientFactoryBuilder logEnabled(boolean logEnabled) {
        dspClientProperty.setLogEnabled(logEnabled);
        return this;
    }

    public DspClientFactoryBuilder classesInput(List<Class> classList) {
        dspClientProperty.setClassesInput(classList);
        return this;
    }

    public DspClientFactoryBuilder datatypes(String datatypes) {
        dspClientProperty.setDatatypes(datatypes);
        return this;
    }

    public  DspClientFactory build() {
        return new DspClientFactory(dspClientProperty);
    }

}
