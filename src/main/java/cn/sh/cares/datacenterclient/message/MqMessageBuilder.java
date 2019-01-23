package cn.sh.cares.datacenterclient.message;

import java.util.Date;

/**
 * 请求消息构建器
 */
public class MqMessageBuilder {
    private MqMessage mqMessage;
    private MqMessageHeader mqMessageHeader;
    private MqMessageBody mqMessageBody;

    public MqMessageBuilder() {
        mqMessage = new MqMessage();
        mqMessageHeader = new MqMessageHeader();
        mqMessageBody = new MqMessageBody();
    }

    public MqMessage build() {
        mqMessage.setHeader(mqMessageHeader);
        mqMessage.setBody(mqMessageBody);
        return mqMessage;
    }

    public MqMessageBuilder sender(String sender) {
        this.mqMessageHeader.setSender(sender);
        return this;
    }

    public MqMessageBuilder sendTime(Date date) {
        this.mqMessageHeader.setSendTime(date);
        return this;
    }

    public MqMessageBuilder receiver(String receiver) {
        this.mqMessageHeader.setReceiver(receiver);
        return this;
    }

    public MqMessageBuilder msgType(String msgType) {
        this.mqMessageHeader.setMsgType(msgType);
        return this;
    }

    public MqMessageBuilder token(String token) {
        this.mqMessageHeader.setToken(token);
        return this;
    }

}
