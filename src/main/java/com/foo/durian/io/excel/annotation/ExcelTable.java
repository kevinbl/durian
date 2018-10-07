package com.foo.durian.io.excel.annotation;

import java.lang.annotation.*;

/**
 * Excel表格注解
 *
 * @author mars.mao created on 2014年10月23日 下午1:56:04
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExcelTable {

    /**
     * Excel文件名
     */
    String fileName();

}