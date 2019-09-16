package cn.sh.cares.dsp;

import cn.sh.cares.dsp.client.DspClientFactoryBuilder;
import cn.sh.cares.dsp.client.IMsgResolver;
import cn.sh.cares.dsp.client.clients.DataApiClient;

/**
 * 数据接口测试
 */
public class DataApiTest {

    public static void main(String[] args) {
        DspClientFactoryBuilder dspClientFactoryBuilder = new DspClientFactoryBuilder();
        DataApiClient dspClient = dspClientFactoryBuilder
                .username("caiyw")
                .password("123456")
                .syscode("TESTCYW")
                .url("http://172.28.31.101:9066/dsp/services")
                .heartBeatInterval(50000L)
                .dataInteval(1000L)
                .datatypes("jkFlight")
                .logEnabled(true)
                .msgResolver(new IMsgResolver() {
            @Override
            public void resolve(String msg) {
                System.out.println(msg);
            }

            @Override
            public String getUniqueSeq() {
                return "1";
            }
        }).build().createDataApiClient();

        dspClient.start();

        System.out.println(dspClient.getApiData("{params:{}}"));


    }


}
