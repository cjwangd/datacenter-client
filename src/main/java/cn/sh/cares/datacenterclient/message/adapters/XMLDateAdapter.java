package cn.sh.cares.datacenterclient.message.adapters;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class XMLDateAdapter extends XmlAdapter<String, Date> {


    private final String format = "yyyy-MM-dd HH:mm:ss";

    /**
     * Convert a value type to a bound type.
     *
     * @param v
     *         The value to be converted. Can be null.
     * @throws Exception
     *         if there's an error during the conversion. The caller is responsible for
     *         reporting the error to the user through {@link ValidationEventHandler}.
     */
    @Override
    public Date unmarshal(String v) throws Exception {
        if (StringUtils.isEmpty(v)) {
            return null;
        }
        DateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.parse(v);
    }

    /**
     * Convert a bound type to a value type.
     *
     * @param v
     *         The value to be convereted. Can be null.
     * @throws Exception
     *         if there's an error during the conversion. The caller is responsible for
     *         reporting the error to the user through {@link ValidationEventHandler}.
     */
    @Override
    public String marshal(Date v) throws Exception {
        if (null == v) {
            return "";
        }
        return DateFormatUtils.format(v, format);
    }
}
