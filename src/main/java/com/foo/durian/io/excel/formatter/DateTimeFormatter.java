package com.foo.durian.io.excel.formatter;

import com.foo.durian.env.constant.DateTimePattern;
import org.joda.time.DateTime;

/**
 * Created by f on 2017/1/9.
 */
public class DateTimeFormatter implements Formatter {
    @Override
    public String format(Object propertyValue) {
        return new DateTime(propertyValue).toString(DateTimePattern.DEFAULT.date());
    }
}
