package cn.sh.cares.dsp.message;

import javax.xml.bind.annotation.*;

/**
 * @author wangcj
 */
@XmlRootElement(name = "Root")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"header", "body"})
public class MqMessage {

    @XmlElement(name = "Header")
    private MqMessageHeader header;

    @XmlElement(name = "Body")
    private MqMessageBody body;


    public MqMessage() {
        body = new MqMessageBody();
    }


    public MqMessageHeader getHeader() {
        return header;
    }

    public void setHeader(MqMessageHeader header) {
        this.header = header;
    }

    public MqMessageBody getBody() {
        return body;
    }


    public void setBody(MqMessageBody body) {
        this.body = body;
    }


    public void setDatas(List list) {
        body.setList(list);
    }

    public void setSeqNum(String seqNum) {
        body.setSeqNum(seqNum);
    }

    public void setDataType(String dataType) {
        body.setDataType(dataType);
    }

    public void setStatus(String status) {
        body.setStatus(status);
    }


}
