
package cn.sh.cares.dsp.message;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
    @XmlElement(name = "DATA", required = true)
    protected DATA data;


    public String getOPERATE() {
        return operate;
    }

    public void setOPERATE(String value) {
        this.operate = value;
    }

    public DATA getDATA() {
        return data;
    }

    public void setDATA(DATA value) {
        this.data = value;
    }

}
