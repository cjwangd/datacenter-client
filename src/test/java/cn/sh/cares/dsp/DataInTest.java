package cn.sh.cares.dsp;

import cn.sh.cares.dsp.client.DspClientFactoryBuilder;
import cn.sh.cares.dsp.client.IMsgResolver;
import cn.sh.cares.dsp.client.clients.DataSaveClient;
import cn.sh.cares.dsp.message.Data;
import cn.sh.cares.dsp.message.Item;
import cn.sh.cares.dsp.message.adapters.XmlDateAdapter;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 数据接入测试
 */
public class DataInTest {

    public static void main(String[] args) {
        DspClientFactoryBuilder dspClientFactoryBuilder = new DspClientFactoryBuilder();
        List<Class> classes = new ArrayList<>();
        classes.add(User.class);
        DataSaveClient dspClient = dspClientFactoryBuilder
                .username("zuo")
                .password("123456")
                .syscode("abs")
                .url("http://172.28.31.1:9066/dsp/services")
                .heartBeatInterval(50000L)
                .dataInterval(1000L)
                .classesInput(classes)  // 数据接入必须参数
                .datatypes("userdatatest")
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
        }).build().createDataSaveClient();
        dspClient.start();


        // 封装要传输的数据列表
        AtomicInteger atomicInteger = new AtomicInteger(1);
        // 在循环体或者定时任务中调用存储数据的方法
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                User user = new User();
                user.setNAME("大事发生");
                user.setSEX("男");
                user.setID(atomicInteger.getAndIncrement());
                user.setBIRTH(new Date());

                List<Item> items = new LinkedList<>();
                Item item = new Item();
                item.setOperate("mod");
                item.setData(user);
                items.add(item);
                System.out.println("发送数据" + items);
                dspClient.putData(items);
            }
        }, 0,5000);

    }


    /**
     * 传输的数据结构
     * 必须继承  Data
     */
    public static class User extends Data {

        String  NAME;
        String SEX;
        Integer ID;

        /**
         * 传输日期要加注解 XmlJavaTypeAdapter
         */
        @XmlJavaTypeAdapter(XmlDateAdapter.class)
        Date BIRTH;

        public String getNAME() {
            return NAME;
        }

        public void setNAME(String NAME) {
            this.NAME = NAME;
        }

        public String getSEX() {
            return SEX;
        }

        public void setSEX(String SEX) {
            this.SEX = SEX;
        }

        public Integer getID() {
            return ID;
        }

        public void setID(Integer ID) {
            this.ID = ID;
        }

        public Date getBIRTH() {
            return BIRTH;
        }

        public void setBIRTH(Date BIRTH) {
            this.BIRTH = BIRTH;
        }
    }
}
