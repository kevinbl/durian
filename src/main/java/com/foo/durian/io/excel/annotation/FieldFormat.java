package com.foo.durian.io.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 该注解用于方法之上, 由方法来定义列的格式化行为
 *
 * Version 1.0.0
 * Created by foolish on 17/2/9.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FieldFormat {

    /**
     * 属性名
     */
    String fieldName();
}
