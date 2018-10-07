package com.foo.durian.io.excel.parser;

/**
 * Version 1.0.0
 * Created by foolish on 16/11/16.
 */
public abstract class DefaultPropertyParser implements PropertyParser<Object> {
    @Override
    public abstract Object parse(Object t) ;
}
