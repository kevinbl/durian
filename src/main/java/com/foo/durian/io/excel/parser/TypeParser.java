package com.foo.durian.io.excel.parser;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * version 1.0.0
 * Created by foolish on 16/9/14 下午5:45.
 */
public class TypeParser {

    private TypeParser() {
    }

    public static final Object parse(String value, Class toType) {

        if (value == null) return null;

        if (String.class == toType) return value;

        if (Boolean.class == toType) return Boolean.parseBoolean(value);

        if (Byte.class == toType || byte.class == toType) return Byte.parseByte(value);
        if (Short.class == toType || short.class == toType) return Short.parseShort(value);
        if (Integer.class == toType || int.class == toType) return Integer.parseInt(value);

        if (Long.class == toType || long.class == toType) return Long.parseLong(value);
        if (Float.class == toType || float.class == toType) return Float.parseFloat(value);
        if (Double.class == toType || double.class == toType) return Double.parseDouble(value);


        if (BigInteger.class == toType) return new BigInteger(value);
        if (BigDecimal.class == toType) return new BigDecimal(value);

        throw new IllegalArgumentException("不支持的类型:" + toType.getName());
    }
}
