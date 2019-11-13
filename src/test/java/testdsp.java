import cn.sh.cares.dsp.client.DspClientFactoryBuilder;
import cn.sh.cares.dsp.client.IMsgResolver;
import cn.sh.cares.dsp.client.clients.DataShareClient;


public class testdsp {
    public static void main(String[] args) {
        DspClientFactoryBuilder dspClientFactoryBuilder = new DspClientFactoryBuilder();
        DataShareClient dspClient = dspClientFactoryBuilder
               
        		.username("UserDevTest")
                .password("test123!")
                .syscode("SysDevTest")
               /* 
        		.username("caiyw")
                .password("123456")
                .syscode("TESTCYW")*/
                .url("http://172.28.31.101:9066/dsp/services")
                .heartBeatInterval(4000L)
                .dataInterval(1000L)
                .datatypes("flightDyn")
                .logEnabled(false)
                .msgResolver(new IMsgResolver() {
            @Override
            public void resolve(String msg) {
            	System.out.println("msg----------------------------------");
                System.out.println(msg);
            }

            @Override
            public String getUniqueSeq() {
                return "1";
            }
        }).build().createDataShareClient();

        dspClient.start();
    }


}
