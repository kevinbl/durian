package com.foo.durian.io.excel;

/**
 * version 1.0.0
 * Created by foolish on 16/8/26 下午12:02.
 */
public interface LineProcessor<T> {
    boolean process(T t);
}
