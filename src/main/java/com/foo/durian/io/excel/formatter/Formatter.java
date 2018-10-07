package com.foo.durian.io.excel.formatter;

/**
 * 对象格式化处理的接口
 *
 * Version 1.0.0
 * Created by f on 16/10/15.
 */
public interface Formatter<T> {

    /**
     * 用于格式化输入对象
     *
     * @param input 输入对象
     * @return 格式化后的字符串
     */
    String format(T input) ;

    /**
     * Marker Class -- 原始Formatter类; 仅用于注解, 将注解中的属性标识为默认值
     */
    abstract class None implements Formatter<Object> {}

    class DefaultFormatter implements Formatter<Object> {
        @Override
        public String format(Object input) {
            return input.toString();
        }
    }
}
