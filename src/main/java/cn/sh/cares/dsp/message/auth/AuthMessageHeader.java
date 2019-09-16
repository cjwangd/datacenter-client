package cn.sh.cares.dsp.message.auth;

import cn.sh.cares.dsp.message.adapters.XmlDateAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;

/**
 * @author wangcj
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class AuthMessageHeader {

	@XmlElement(name = "Sender",required = true)
	private String sender;
	
	@XmlElement(name = "SendTime")
	@XmlJavaTypeAdapter(XmlDateAdapter.class)
	private Date sendTime;
	 
	@XmlElement(name = "Receiver")
	private String receiver;

	@XmlElement(name = "AuthType")
	private String authType;


	public String getSender() {
		return sender;
	}
	
	public void setSender(String sender) {
		this.sender = sender;
	}

	
	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}


	public String getReceiver() {
		return receiver;
	}

	public void setReceiver(String receiver) {
		this.receiver = receiver;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}
}

