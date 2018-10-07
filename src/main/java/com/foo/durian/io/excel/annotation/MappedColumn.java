package com.foo.durian.io.excel.annotation;

import com.foo.durian.io.excel.parser.DefaultPropertyParser;
import com.foo.durian.io.excel.parser.PropertyParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Version 1.0.0 Created by foolish on 16/11/16.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MappedColumn {
    /**
     * 固定列名需要显示指定映射表头的名称，以是否为默认值来决定表头的映射关系解析方式
     */
    String name() default "";

    Class<? extends PropertyParser> parserClass() default DefaultPropertyParser.class;
}
