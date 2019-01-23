package cn.sh.cares.datacenterclient.message;

import javax.xml.bind.annotation.*;
import java.util.List;


@XmlRootElement(name = "Root")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"header", "body"})
public class MqMessage<T> {

    @XmlElement(name = "Header")
    private MqMessageHeader header;

    @XmlElement(name = "Body")
    private MqMessageBody<T> body;


    public MqMessage() {
        body = new MqMessageBody<T>();
    }


    public MqMessageHeader getHeader() {
        return header;
    }

    public void setHeader(MqMessageHeader header) {
        this.header = header;
    }

    public MqMessageBody<T> getBody() {
        return body;
    }


    public void setBody(MqMessageBody<T> body) {
        this.body = body;
    }


    public void setDatas(List<T> list) {
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
