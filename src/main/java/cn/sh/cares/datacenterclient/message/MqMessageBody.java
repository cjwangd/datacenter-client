package cn.sh.cares.datacenterclient.message;

import javax.xml.bind.annotation.*;
import java.util.List;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"seqNum", "dataType", "status", "list"})
public class MqMessageBody<T> {

    @XmlElement(name = "SeqNum")
    private String seqNum;

    @XmlElement(name = "DataType")
    private String dataType;

    @XmlElement(name = "Status", nillable = true)
    private String status;

    @XmlElementWrapper(name = "List", nillable = true)
    @XmlElement(name = "DATA", nillable = true)
    private List<T> list;


    public String getSeqNum() {
        return seqNum;
    }

    public void setSeqNum(String seqNum) {
        this.seqNum = seqNum;
    }


    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }


}
