package cn.sh.cares.datacenterclient.common;

public class MqMessageConstant {


    public static final String DEFAULT_QUEUE_IN = "datacenter_queue_in";
    public static final String SENDSYS_CODE = "dataCenter";


    /**
     * 消心状态
     *
     * @author 王长金
     */
    public enum MqMessageStatus {

        /**
         * 用于心跳响应消息，表示服务正常可用
         **/
        UP("Up"),
        /**
         * 用于心跳响应消息，表示服务不可用
         **/
        DOWN("Down"),
        ACCEPT("Accept"),
        REJECT("Reject"),
        UNAVAILABLE("Unavailable"),
        ;

        private String status;

        MqMessageStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }


    /**
     * 消息参与者
     *
     * @author 王长金
     */
    public enum Participate {

        DATACENTER(SENDSYS_CODE);

        private String participate;

        Participate(String participate) {
            this.participate = participate;
        }

        public String getParticipate() {
            return participate;
        }
    }

    public class MsgType {

        //订阅请求消息
        public static final String SUBSCRIBE_REQUEST = "subScribeRequest";

        //订阅应答消息
        public static final String SUBSCRIBE_RESPONES = "subScribeRespones";

        //心跳请求消息
        public static final String HEARTBEAT_REQUEST = "heartBeatRequest";

        //心跳应答消息
        public static final String HEARTBEAT_RESPONES = "heartBeatRespones";

    }

    public class DataType {

        public static final String SIMS_SCS = "simsScs";

        public static final String SIMS_BGC = "simsBgc";

        public static final String SIMS_JYXLXX = "simsJyxlxx";

        public static final String SIMS_LKXX = "simsLkxx";

        public static final String IMF_FLIGHT = "imfFlight";

    }

    public class SubscribeStaus {

        //准备接受
        public static final String ACCEPT = "1";

    }


}
