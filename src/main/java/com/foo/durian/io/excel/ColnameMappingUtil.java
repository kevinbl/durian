package com.foo.durian.io.excel;

import com.foo.durian.io.excel.parser.DefaultPropertyParser;
import com.google.common.collect.Maps;
import com.foo.durian.io.FileLineCell;
import com.foo.durian.io.excel.annotation.MappedColumn;
import com.foo.durian.io.excel.parser.PropertyParser;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Version 1.0.0 Created by foolish on 16/11/16.
 */
public class ColnameMappingUtil {

    private static final Logger logger = LoggerFactory.getLogger(ColnameMappingUtil.class);

    public static <M> FileLineCell[] toFileLineCells(M colnameMapping) {
        Map<String, String> colNameMap = getColnameMap(colnameMapping);
        Map<String, PropertyParser> propertyParserMap = getPropertyParserMap(colnameMapping);

        FileLineCell[] cells = new FileLineCell[colNameMap.size()];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = new FileLineCell();
        }

        int index = 0;
        for (Map.Entry<String, String> colNameEntry: colNameMap.entrySet()) {
            String classFieldName = colNameEntry.getKey();
            String title = colNameEntry.getValue();

            cells[index].setClassFieldName(classFieldName);
            cells[index].setName(title);
            // 特殊类型转换器
            PropertyParser parser = propertyParserMap.get(classFieldName);
            if (parser != null) {
                cells[index].setPropertyParser(parser);
            }

            index ++;
        }

        return cells;
    }

    public static <M> Map<String, String> getColnameMap(M colnameMapping) {
        Map<String, String> colnameMap = Maps.newHashMap();
        Class<?> clazz = colnameMapping.getClass();
        for (Field field : getFields(clazz)) {
            MappedColumn mappedColumn = field.getAnnotation(MappedColumn.class);
            if (mappedColumn != null) {
                // TD ReflectUtil
                if (StringUtils.isNotBlank(mappedColumn.name())) {
                    colnameMap.put(field.getName(), mappedColumn.name());
                    continue;
                }
                String columnNameValue = (String) getFieldValue(colnameMapping, field);
                if (!StringUtils.isBlank(columnNameValue)) { // 忽略空的列名
                    colnameMap.put(field.getName(), columnNameValue);
                }
            }
        }
        return colnameMap;
    }

    public static <M> Map<String, PropertyParser> getPropertyParserMap(M colnameMapping) {
        Map<String, PropertyParser> propertyParserMap = Maps.newHashMap();
        Class<?> clazz = colnameMapping.getClass();
        for (Field field : getFields(clazz)) {
            MappedColumn mappedColumn = field.getAnnotation(MappedColumn.class);
            if (mappedColumn != null) {
                Class<? extends PropertyParser> parserClass = mappedColumn.parserClass();
                if (!parserClass.equals(DefaultPropertyParser.class)) {
                    propertyParserMap.put(field.getName(), getInstance(parserClass));
                }
            }
        }
        return propertyParserMap;
    }

    private static Field[] getFields(Class<?> clazz) {
        return clazz.getDeclaredFields();
    }

    private static <M> Object getFieldValue(M obj, Field field) {
        try {
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            logger.error("通过反射获取对象字段值失败: field={}", field, e);
            throw new RuntimeException("通过反射获取对象字段值失败");
        }
    }

    private static PropertyParser getInstance(Class<? extends PropertyParser> parserClass) {
        try {
            return parserClass.newInstance();
        } catch (Exception e) {
            logger.error("通过反射生成PropertyParser对象失败, parserClass={}", parserClass);
            throw new RuntimeException("通过反射生成PropertyParser对象失败");
        }
    }
}
