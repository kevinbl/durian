package com.foo.durian.io.excel.formatter;

import org.joda.time.DateTime;

import java.sql.Timestamp;

/**
 * version 1.0.0
 * Created by foolish on 2017/4/25 下午6:58.
 */
public class TimestampFormatter implements Formatter<Timestamp> {
    @Override
    public String format(Timestamp input) {
        return input == null ? null : new DateTime(input.getTime()).toString("yyyy-MM-dd");
    }
}
