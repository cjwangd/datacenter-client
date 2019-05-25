package cn.sh.cares.dsp.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public class DateUtil {
    private static final String DT_FMT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static DateTimeFormatter dateTimeFormatter;
    static {
        dateTimeFormatter = DateTimeFormatter.ofPattern(DT_FMT_PATTERN, Locale.CHINA);
    }

    public static String formatDate(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        return localDateTime.format(dateTimeFormatter);
    }

    public static Date parseDate(String datestr) {
        return Date.from(LocalDateTime.from(dateTimeFormatter.parse(datestr)).atZone(ZoneId.systemDefault()).toInstant());
    }


}
