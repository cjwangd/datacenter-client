
package cn.sh.cares.dsp.message;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

/**
 * @author wangcj
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "item"
})
@XmlRootElement(name = "List")
public class List {

    @XmlElement(name = "Item", required = true)
    protected java.util.List<Item> item;


    public java.util.List<Item> getItem() {
        if (item == null) {
            item = new ArrayList<>();
        }
        return this.item;
    }

    public void setItem(java.util.List<Item> item) {
        this.item = item;
    }
}
