
package cn.sh.cares.dsp.message;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author wangcj
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "operate",
    "data"
})
@XmlRootElement(name = "Item")
public class Item {

    @XmlElement(name = "OPERATE", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "NCName")
    protected String operate;
    @XmlElement(name = "Data", required = true)
    protected Data data;


    public String getOperate() {
        return operate;
    }

    public void setOperate(String value) {
        this.operate = value;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data value) {
        this.data = value;
    }

}
