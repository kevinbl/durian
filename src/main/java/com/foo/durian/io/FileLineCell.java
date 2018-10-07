package com.foo.durian.io;

import com.foo.durian.io.excel.parser.PropertyParser;

/**
 * 不把这个类定义成annotation 是因为考虑到一个类可能给多个表用，每个表的定义都不一样
 * version 1.0.0
 * Created by f on 16/8/1 下午7:33.
 */
public class FileLineCell {


    /**
     * excel中对应header 的名称
     */
    private String name;
    /**
     * 要映射的类的字段的名称
     */
    private String classFieldName;

    /**
     * 将字段转换为对应的 属性类型
     * 非必须，缺省使用默认
     * 可能需要的情况，某个字段可能出现多个类型
     * 一些poi没有支持的类型，比如bigdecimal
     */
    private PropertyParser propertyParser;

    public FileLineCell(){}

    public FileLineCell(String name, String classFieldName){
        this.name=name;
        this.classFieldName=classFieldName;
    }
    public FileLineCell(String name, String classFieldName, PropertyParser propertyParser){
        this.name=name;
        this.classFieldName=classFieldName;
        this.propertyParser=propertyParser;
    }




    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassFieldName() {
        return classFieldName;
    }

    public void setClassFieldName(String classFieldName) {
        this.classFieldName = classFieldName;
    }

    public PropertyParser getPropertyParser() {
        return propertyParser;
    }

    public void setPropertyParser(PropertyParser propertyParser) {
        this.propertyParser = propertyParser;
    }

    @Override
    public String toString() {
        return "FileLineCell{" +
                "name='" + name + '\'' +
                ", classFieldName='" + classFieldName + '\'' +
                ", propertyParser=" + propertyParser +
                '}';
    }
}
