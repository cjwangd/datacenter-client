package cn.sh.cares.datacenterclient.message.auth;

import javax.xml.bind.annotation.*;


@XmlRootElement(name="Root",namespace = "loginauth")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"header","body"})
public class AuthMessage {
	
	@XmlElement(name = "Header")
	private AuthMessageHeader header;
	
	@XmlElement(name = "Body")
	private AuthMessageBody body;

	
	public AuthMessage(){
		body = new AuthMessageBody();
	}
	

	public AuthMessageHeader getHeader() {
		return header;
	}

	public void setHeader(AuthMessageHeader header) {
		this.header = header;
	}

	public AuthMessageBody getBody() {
		return body;
	}


	public void setBody(AuthMessageBody body) {
		this.body = body;
	}
	
	
	
}
