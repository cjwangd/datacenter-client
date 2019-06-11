package cn.sh.cares.dsp.utils;

/**
 * @author wangcj
 */
public class StringUtil {
    private StringUtil(){}

    public static boolean isEmpty(String string) {

        return null == string || "".equals(string.trim());
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }
}
