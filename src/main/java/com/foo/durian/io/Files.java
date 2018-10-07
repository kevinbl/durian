package com.foo.durian.io;

import com.google.common.base.Splitter;

import java.io.File;
import java.util.List;

/**
 * version 1.0.0
 * Created by foolish on 16/8/1 下午8:21.
 */
@Deprecated
public class Files {

    private static final Splitter nameSplitter = Splitter.on(".");

    public static String getSuffix(File file) {
        List<String> list = nameSplitter.splitToList(file.getName());
        if (list.size() >= 2) {
            return list.get(list.size() - 1);
        }
        return null;
    }


}
