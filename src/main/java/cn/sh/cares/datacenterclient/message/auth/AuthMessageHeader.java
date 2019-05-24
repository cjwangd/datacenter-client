package cn.sh.cares.datacenterclient.message.auth;

import cn.sh.cares.datacenterclient.message.adapters.XMLDateAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Date;


@XmlAccessorType(XmlAccessType.FIELD)
public class AuthMessageHeader {

	@XmlElement(name = "Sender",required = true)
	private String sender;
	
	@XmlElement(name = "SendTime")
	@XmlJavaTypeAdapter(XMLDateAdapter.class)
	private Date sendTime;
	 
	@XmlElement(name = "Receiver")
	private String receiver;
	 


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



}

