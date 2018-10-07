package com.foo.durian.io.excel;

import com.google.common.collect.Lists;
import com.foo.durian.io.excel.formatter.Formatter;

import java.io.Serializable;
import java.util.List;

/**
 * 动态生成Excel，列记录。 Created by foolish on 2017/1/9.
 */
@SuppressWarnings("all")
public class ColumnRecord implements Serializable {

    private static final long serialVersionUID = -5207043025535367719L;
    /**
     * 多层级表头，可以打印多行复合表头。
     */
    private List<String> columnNameList = Lists.newArrayList();

    /**
     * 单元格数据
     */
    private Object propertyValue;

    /**
     * 数据格式转换器
     */
    private Class<? extends Formatter> formatter = Formatter.DefaultFormatter.class;

    public ColumnRecord() {
    }

    public ColumnRecord(ColumnRecord columnRecord) {
        this.columnNameList = Lists.newArrayList();
        for (String columnName : columnRecord.getColumnNameList()) {
            columnNameList.add(columnName);
        }
        this.propertyValue = columnRecord.getPropertyValue();
        this.formatter = columnRecord.getFormatter();
    }

    public ColumnRecord(List<String> columnNameList, Object propertyValue) {
        this.columnNameList = columnNameList;
        this.propertyValue = propertyValue;
        this.formatter = Formatter.DefaultFormatter.class;
    }

    public ColumnRecord(List<String> columnNameList, Object propertyValue, Class<? extends Formatter> formatter) {
        this.columnNameList = columnNameList;
        this.propertyValue = propertyValue;
        this.formatter = formatter;
    }

    public List<String> getColumnNameList() {
        return columnNameList;
    }

    public void setColumnNameList(List<String> columnNameList) {
        this.columnNameList = columnNameList;
    }

    public Object getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(Object propertyValue) {
        this.propertyValue = propertyValue;
    }

    public Class<? extends Formatter> getFormatter() {
        return formatter;
    }

    public void setFormatter(Class<? extends Formatter> formatter) {
        this.formatter = formatter;
    }
}