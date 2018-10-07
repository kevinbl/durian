package com.foo.durian.io.excel.formatter;

import org.joda.time.DateTime;

/**
 * Created by foolish on 2016/11/24.
 */
public class DateTimeDayFormatter implements Formatter<DateTime> {
    @Override
    public String format(DateTime dateTime) {
        return dateTime.toString("yyyy-MM-dd");
    }

}
