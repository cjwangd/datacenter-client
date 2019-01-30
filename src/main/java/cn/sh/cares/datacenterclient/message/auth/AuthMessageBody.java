package cn.sh.cares.datacenterclient.message.auth;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder={"code","message","token","userName","passWord","ip"})
public class AuthMessageBody {

	@XmlElement(name = "Code")
	private String code;
	
	@XmlElement(name = "Message")
	private String message;
		
	@XmlElement(name = "UserName")
	private String userName;
	
	@XmlElement(name = "PassWord")
	private String passWord;
	
	@XmlElement(name = "Ip")
	private String ip;
	
	@XmlElement(name = "Token")
	private String token;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}
		
	
	
}
