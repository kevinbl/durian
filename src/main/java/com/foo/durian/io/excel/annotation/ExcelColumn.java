package com.foo.durian.io.excel.annotation;

import com.foo.durian.io.excel.TableFormatterConfigurer;
import com.foo.durian.io.excel.formatter.Formatter;
import com.foo.durian.io.excel.view.ExcelView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Excel列属性注解
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelColumn {

    /**
     * 列标题
     */
    String headerName();

    /**
     * 列宽度
     */
    int columnWidth() default 15;

    /**
     * 字段格式
     */
    String pattern() default "";

    /**
     * 列格式处理器
     */
    Class<? extends Formatter> formatterClass() default Formatter.None.class;

    /**
     * 标志位: 是否允许使用可供替换的Formatter
     * <p>
     *     默认false;
     *     置为true时, 会尝试查找临时注册的Formatter来替换默认的Formatter
     * </p>
     * @see ExcelView
     * @see TableFormatterConfigurer
     */
    boolean allowAlternativeFormatter() default false;
}
