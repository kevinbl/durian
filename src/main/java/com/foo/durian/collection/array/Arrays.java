package com.foo.durian.collection.array;

/**
 * 数组相关
 * version 1.0.0
 * Created by foolish on 16/8/21 下午5:58.
 */
public class Arrays {

    public static <T> boolean isEmpty(T[] t) {
        return t == null || t.length == 0;
    }

    public static <T> boolean isNotEmpty(T[] t) {
        return !isEmpty(t);

    }
}
