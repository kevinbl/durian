package com.foo.durian.io.excel.parser;

import com.foo.durian.env.constant.DateTimePattern;
import org.joda.time.DateTime;

import java.util.Date;

/**
 * 将任意类型转换成string
 * version 1.0.0
 * Created by foolish on 16/8/2 下午12:05.
 * updated by foolish
 */
public class StringPropertyParser implements PropertyParser<String> {
    public String parse(Object t) {
        if(t==null){
            return null;
        }
        if (t instanceof Date) {
            DateTime dateTime = new DateTime(t);
            return dateTime.toString(DateTimePattern.DEFAULT.datetime());
        } else if (t instanceof Double) {
            return t.toString();
        }
        return t.toString();
    }
}