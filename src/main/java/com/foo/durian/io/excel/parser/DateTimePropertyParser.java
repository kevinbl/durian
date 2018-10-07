package com.foo.durian.io.excel.parser;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.util.Date;

/**
 * Version 1.0.0 Created by foolish on 16/11/16.
 */
public class DateTimePropertyParser implements PropertyParser<DateTime> {
    @Override
    public DateTime parse(Object t) {
        if (t == null) {
            return null;
        }

        if (t instanceof Date) {
            return new DateTime(t);
        }

        String s = String.valueOf(t).trim();
        /*
         * 日期解析需要考虑不同格式(目前支持4种)
         */
        String dtFormat = "yyyy-MM-dd"; // 年月日的格式串: yyyy-MM-dd
        if (s.contains("/")) {
            dtFormat = "yyyy/MM/dd"; // 年月日的格式串: yyyy/MM/dd
        }
        if (s.length() > 10) {
            dtFormat += " HH:mm:ss";
        }
        return DateTime.parse(s, DateTimeFormat.forPattern(dtFormat));
    }
}
