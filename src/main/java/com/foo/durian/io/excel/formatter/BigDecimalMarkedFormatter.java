package com.foo.durian.io.excel.formatter;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by f on 2017/2/10.
 */
public class BigDecimalMarkedFormatter implements Formatter<BigDecimal> {
    private static DecimalFormat df = new DecimalFormat("#,###.00");
    @Override
    public String format(BigDecimal input) {
        df.setPositivePrefix("+");
        return df.format(input);
    }
}
