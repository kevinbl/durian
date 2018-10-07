package com.foo.durian.io.excel.parser;

import com.google.common.base.Strings;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;

/**
 * 金融数字，三位一个逗号
 *
 * Created by foolish on 2017/2/10.
 */
public class FCBigDecimalPropertyPaser implements PropertyParser<BigDecimal> {
    @Override
    public BigDecimal parse(Object t) {
        DecimalFormat df = new DecimalFormat("#,##0.00");
        if(t==null||Strings.isNullOrEmpty(t.toString())){
            return null;
        }
        String v=String.valueOf(t);
        Object o;
        try {
            o = df.parseObject(v);
        } catch (ParseException e) {
            throw new RuntimeException("解析错误");
        }
        return new BigDecimal(o.toString());

    }
}
