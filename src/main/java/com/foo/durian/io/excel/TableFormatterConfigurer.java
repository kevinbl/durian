package com.foo.durian.io.excel;

import com.foo.durian.io.excel.formatter.Formatter;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/**
 * 用于存储&传递格式化相关的配置信息 TIPS: 一个可能更合理的实现方式是使用Builder模式构造配置项
 *
 * Version 1.0.0 Created by f on 16/10/15.
 */
public class TableFormatterConfigurer {

    public static TableFormatterConfigurer NONE = new TableFormatterConfigurer();

    /**
     * 需要过滤的header列表
     */
    private final Set<String> headersToBeFiltered;
    /**
     * 需要替换的header列表, 以映射的方式给出
     */
    private final Map<String, String> headersMapping;
    /**
     * 注册的FormatterMap, 以类型为key
     */
    private final Map<Class<?>, Formatter<?>> formatterByClass;
    /**
     * 注册的FormatterMap, 以属性名为key
     */
    private final Map<String, Formatter<?>> formatterByFieldName;

    public TableFormatterConfigurer() {
        headersToBeFiltered = Sets.newHashSet();
        headersMapping = Maps.newHashMap();
        formatterByClass = Maps.newHashMap();
        formatterByFieldName = Maps.newHashMap();
    }

    @Deprecated // 推荐使用链式调用构造配置项
    public TableFormatterConfigurer(Set<String> headersToBeFiltered) {
        this(headersToBeFiltered, Maps.<Class<?>, Formatter<?>> newHashMap(), Maps.<String, Formatter<?>> newHashMap());
    }

    @Deprecated // 推荐使用链式调用构造配置项
    public TableFormatterConfigurer(Set<String> headersToBeFiltered, Map<Class<?>, Formatter<?>> formatterByClass,
            Map<String, Formatter<?>> formatterByFieldName) {
        this(headersToBeFiltered, Maps.<String, String> newHashMap(), formatterByClass, formatterByFieldName);
    }

    @Deprecated
    public TableFormatterConfigurer(Set<String> headersToBeFiltered, Map<String, String> headersMapping,
            Map<Class<?>, Formatter<?>> formatterByClass, Map<String, Formatter<?>> formatterByFieldName) {
        this.headersToBeFiltered = headersToBeFiltered;
        this.headersMapping = headersMapping;
        this.formatterByClass = formatterByClass;
        this.formatterByFieldName = formatterByFieldName;
    }

    public TableFormatterConfigurer filterHeaders(Iterable<String> headers) {
        for (String header : headers) {
            filterHeader(header);
        }
        return this;
    }

    public TableFormatterConfigurer filterHeader(String header) {
        headersToBeFiltered.add(header);
        return this;
    }

    public TableFormatterConfigurer replaceHeaders(Map<String, String> headersMapping) {
        for (Map.Entry<String, String> entry : headersMapping.entrySet()) {
            replaceHeader(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public TableFormatterConfigurer replaceHeader(String oldHeader, String newHeader) {
        headersMapping.put(oldHeader, newHeader);
        return this;
    }

    public <T> TableFormatterConfigurer registerFormattersByClass(Map<Class<T>, Formatter<T>> formatterMap) {
        Preconditions.checkNotNull(formatterMap, "formatterMap不能为NULL");
        for (Map.Entry<Class<T>, Formatter<T>> entry : formatterMap.entrySet()) {
            Class<T> clazz = entry.getKey();
            Formatter<T> formatter = entry.getValue();
            registerFormatter(clazz, formatter);
        }
        return this;
    }

    public TableFormatterConfigurer registerFormattersByFieldName(Map<String, Formatter<?>> formatterMap) {
        Preconditions.checkNotNull(formatterMap, "formatterMap不能为NULL");
        for (Map.Entry<String, Formatter<?>> entry : formatterMap.entrySet()) {
            String fieldName = entry.getKey();
            Formatter<?> formatter = entry.getValue();
            registerFormatter(fieldName, formatter);
        }
        return this;
    }

    public <T> TableFormatterConfigurer registerFormatter(Class<T> clazz, Formatter<T> formatter) {
        Preconditions.checkNotNull(clazz, "class不能为NULL");
        Preconditions.checkNotNull(formatter, "formatter不能为NULL");
        formatterByClass.put(clazz, formatter);
        return this;
    }

    public TableFormatterConfigurer registerFormatter(String fieldName, Formatter<?> formatter) {
        Preconditions.checkNotNull(fieldName, "fieldName不能为NULL");
        Preconditions.checkNotNull(formatter, "formatter不能为NULL");
        formatterByFieldName.put(fieldName, formatter);
        return this;
    }

    public Set<String> getHeadersToBeFiltered() {
        return headersToBeFiltered;
    }

    public Map<String, String> getHeadersMapping() {
        return headersMapping;
    }

    /**
     * 根据给定class对象获取对应类型的Formatter -- 该方法无类型转换异常, 因为通过class对象注册formatter的方法使用泛型限定了类型的一致性的
     *
     * @param clazz T类型的class对象
     * @param <T>
     * @return 适用于T类型的Formatter(如果有的话); 否则返回null
     */
    @SuppressWarnings("unchecked")
    public <T> Formatter<T> getFormatter(Class<T> clazz) {
        return (Formatter<T>) formatterByClass.get(clazz);
    }

    /**
     * 根据给定属性名获取对应的Formatter -- 该方法(在错误操作的情况下)可能导致类型转换异常, 因属性名无类型信息, 与之绑定的Formatter可以处理的类型可能与该属性的类型不一致
     *
     * @param fieldName 属性名
     * @param <T>
     * @return 与属性名绑定的Formatter(如果有的话); 否则返回null
     */
    @SuppressWarnings("unchecked")
    public <T> Formatter<T> getFormatter(String fieldName) {
        return (Formatter<T>) formatterByFieldName.get(fieldName);
    }
}
