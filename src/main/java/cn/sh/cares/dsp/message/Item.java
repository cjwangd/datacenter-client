
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


    public String getOPERATE() {
        return operate;
    }

    public void setOPERATE(String value) {
        this.operate = value;
    }

    public Data getDATA() {
        return data;
    }

    public void setDATA(Data value) {
        this.data = value;
    }

}
