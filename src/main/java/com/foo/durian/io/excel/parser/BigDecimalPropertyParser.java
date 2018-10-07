package com.foo.durian.io.excel.parser;

import java.math.BigDecimal;

/**
 * version 1.0.0
 * Created by foolish on 16/8/17 下午4:10.
 */
public class BigDecimalPropertyParser implements PropertyParser<BigDecimal> {

    public BigDecimal parse(Object t) {
        if(t==null){
            return null;
        }
        String v=String.valueOf(t);
        return new BigDecimal(v);
    }
}
