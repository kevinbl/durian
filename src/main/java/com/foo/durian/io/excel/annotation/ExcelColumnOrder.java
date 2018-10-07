package com.foo.durian.io.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel列顺序注解
 *
 * @author foolish
 * @version v1.0.0
 * @since 17/3/20
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcelColumnOrder {

    String[] orderByFields() default {};
}
