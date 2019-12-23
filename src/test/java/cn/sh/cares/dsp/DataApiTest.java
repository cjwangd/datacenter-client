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

        dspClient.start();

        System.out.println(dspClient.getApiData(
                "{" +
                        "params:" +
                        "{" +
                        "   \"flightno\":\"HU7272\"," +         // 航班号必填
                        "   \"flightdate\":\"2019-11-07\"" +   // 日期不填，默认当天
                        "}" +
                        "}"));

        /**  样例返回
         *   {
         *      "code":"0",  非0 失败
         *      "data":{
         *                  "checkIn":157,   // 值机人数
         *                  "takeOff":0,     // 登机人数
         *                  "securityCheck":142     // 安检人数
         *              }
         *  }
         */


    }


}
