package cn.sh.cares.dsp.message.auth;

import cn.sh.cares.dsp.common.MqMessageConstant;

import java.util.Date;

/**
 * 登录消息构建
 * @author wangcj
 */
public class AuthMessageBuilder {

    private AuthMessage authMessage;
    private AuthMessageHeader authMessageHeader;
    private AuthMessageBody authMessageBody;

    public AuthMessageBuilder() {
        authMessage = new AuthMessage();
        authMessageBody = new AuthMessageBody();
        authMessageHeader = new AuthMessageHeader();
    }

    public AuthMessage build() {
        authMessage.setBody(authMessageBody);
        authMessage.setHeader(authMessageHeader);
        authMessageHeader.setReceiver(MqMessageConstant.Participate.DATACENTER.getParticipateName());
        authMessageHeader.setSendTime(new Date());
        return authMessage;
    }

    public AuthMessageBuilder username(String userName) {
        authMessageBody.setUserName(userName);
        return this;
    }

    public AuthMessageBuilder password(String password) {
        authMessageBody.setPassWord(password);
        return this;
    }

    public AuthMessageBuilder sender(String sender) {
        authMessageHeader.setSender(sender);
        return this;
    }
}
