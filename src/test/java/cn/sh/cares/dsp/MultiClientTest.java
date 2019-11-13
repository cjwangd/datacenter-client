package cn.sh.cares.dsp;

import cn.sh.cares.dsp.client.DspClientFactoryBuilder;
import cn.sh.cares.dsp.client.IMsgResolver;
import cn.sh.cares.dsp.client.clients.DataApiClient;
import cn.sh.cares.dsp.client.clients.DataShareClient;

public class MultiClientTest {


    public static void main(String[] args) {
        DspClientFactoryBuilder dspClientFactoryBuilder = new DspClientFactoryBuilder();
        DataShareClient dspClient = dspClientFactoryBuilder
                .username("caiyw")
                .password("123456")
                .syscode("TESTCYW")
                .url("http://172.28.31.101:9066/dsp/services")
                .heartBeatInterval(50000L)
                .dataInterval(1000L)
                .datatypes("flightDyn")
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
                }).build().createDataShareClient();

        dspClient.start();


        DspClientFactoryBuilder dspClientFactoryBuilder1 = new DspClientFactoryBuilder();
        DataApiClient dataApiClient = dspClientFactoryBuilder1
                .username("caiyw")
                .password("123456")
                .syscode("TESTCYW")
                .url("http://172.28.31.1:9066/dsp/services")
                .heartBeatInterval(50000L)
                .dataInterval(1000L)
                .datatypes("jkRtPsgForFlight")
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

        dataApiClient.start();


        new Thread(() -> {
            System.out.println(dataApiClient.getApiData(
                    "{" +
                            "params:" +
                            "{" +
                            "   \"flightno\":\"HU7272\"," +         // 航班号必填
                            "   \"flightdate\":\"2019-11-07\"" +   // 日期不填，默认当天
                            "}" +
                            "}"));
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();


    }
}
