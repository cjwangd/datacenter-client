package cn.sh.cares.dsp.utils;

public class StringUtil {
    public static boolean isEmpty(String string) {

        return null == string || "".equals(string.trim());
    }

    public static boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }
}
