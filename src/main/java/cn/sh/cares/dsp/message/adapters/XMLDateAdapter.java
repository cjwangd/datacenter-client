package cn.sh.cares.dsp.message.adapters;

import cn.sh.cares.dsp.utils.DateUtil;
import cn.sh.cares.dsp.utils.StringUtil;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Date;

public class XMLDateAdapter extends XmlAdapter<String, Date> {


    /**
     * Convert a value type to a bound type.
     *
     * @param v
     *         The value to be converted. Can be null.
     */
    @Override
    public Date unmarshal(String v) {
        if (StringUtil.isEmpty(v)) {
            return null;
        }
        return DateUtil.parseDate(v);
    }

    /**
     * Convert a bound type to a value type.
     *
     * @param v
     *         The value to be convereted. Can be null.
     */
    @Override
    public String marshal(Date v) {
        if (null == v) {
            return "";
        }
        return DateUtil.formatDate(v);
    }
}
