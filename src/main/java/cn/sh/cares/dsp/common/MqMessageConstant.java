package cn.sh.cares.dsp.common;

/**
 * @author wangcj
 */
public class MqMessageConstant {


	public class MsgType{

		private MsgType(){}

		/** 订阅请求消息 */
		public static final String SUBSCRIBE_REQUEST="subScribeRequest";

		/** 订阅应答消息 */
		public static final String SUBSCRIBE_RESPONES="subScribeResponse";


		/** 订阅取消请求消息 */
		public static final String SUBSCRIBE_C_REQUEST="subScribeCancelRequest";

		/** 订阅取消应答消息 */
		public static final String SUBSCRIBE_C_RESPONES="subScribeCancelResponse";



		/** 心跳请求消息 */
		public static final String HEARTBEAT_REQUEST="heartBeatRequest";

		/** 心跳应答消息 */
		public static final String HEARTBEAT_RESPONES="heartBeatResponse";


		/** 数据请求消息 */
		public static final String DATA_REQUEST="dataRequest";

		/** 数据应答消息 */
		public static final String DATA_RESPONES="dataResponse";

		/** 无数据  应答消息 */
		public static final String NO_DATA_RESPONSE = "noDataResponse";



	}


	public class DataType{

		private DataType() {

		}

		public static final String SIMS_SCS = "simsScs";

		public static final String SIMS_BGC = "simsBgc";

		public static final String SIMS_JYXLXX = "simsJyxlxx";

		public static final String SIMS_LKXX = "simsLkxx";

		public static final String IMF_FLIGHT = "imfFlight";

	}


	public static final String DEFAULT_QUEUE_IN = "dsp_queue_in";


	public static final String SENDSYS_CODE ="dsp";

	public class SubscribeStaus{

		private SubscribeStaus() {

		}

		/** 初始化 */
		public static final String INIT = "0";

		/** 准备接受 */
		public static final String ACCEPT = "1";

	}

	/**
	 * 消心状态
	 * @author 王长金
	 */
	public enum MqMessageStatus {

		/**用于心跳响应消息，表示服务正常可用**/
		UP("Up"),
		/**用于心跳响应消息，表示服务不可用**/
		DOWN("Down"),
		ACCEPT("Accept"),
		REJECT("Reject"),
		UNAVAILABLE("Unavailable"),
		/**token过期**/
		TOKENEXPIRE("TokenExpire"),
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
	 * @author 王长金
	 */
	public enum Participate {

		/**
		 * 数据共享平台
		 */
		DATACENTER(SENDSYS_CODE);

		Participate(String participate) {
			this.participateName = participate;
		}

		private String participateName;

		public String getParticipateName() {
			return participateName;
		}
	}


}

