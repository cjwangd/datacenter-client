package cn.sh.cares.dsp.client;

import cn.sh.cares.dsp.client.clients.DataApiClient;
import cn.sh.cares.dsp.client.clients.DataSaveClient;
import cn.sh.cares.dsp.client.clients.DataShareClient;
/**
 * 客户端工厂
 * @author wangcj
 */
public class DspClientFactory {

    private DspClientProperty dspClientProperty;


    public DspClientFactory(DspClientProperty dspClientProperty) {
        this.dspClientProperty = dspClientProperty;
    }

    public DataSaveClient createDataSaveClient() {
        return new DataSaveClient(dspClientProperty);
    }

    public DataShareClient createDataShareClient() {
        return new DataShareClient(dspClientProperty);
    }

    public DataApiClient createDataApiClient() {
        return new DataApiClient(dspClientProperty);
    }

}
