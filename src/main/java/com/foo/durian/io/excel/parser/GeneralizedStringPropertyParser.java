package com.foo.durian.io.excel.parser;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * Created by f on 2017/2/17.
 */
public class GeneralizedStringPropertyParser implements PropertyParser<String> {
    @Override
    public String parse(Object t) {
        if (t instanceof Date) {
            DateTime dateTime = new DateTime(t);
            return dateTime.toString("yyyy-MM-dd");
        } else if (t instanceof Double) {
            return t.toString();
        }
        return t.toString();
    }
}
