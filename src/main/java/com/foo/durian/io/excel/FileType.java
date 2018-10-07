package com.foo.durian.io.excel;

/**
 * version 1.0.0
 * Created by foolish on 16/8/1 下午7:47.
 */
public enum FileType {

    excel2003("xls","2003"),
    excel2007("xlsx","2007"),
    csv("csv","csv");

    public final String suffix;
    public final String desc;

    private FileType(String suffix, String desc){
        this.suffix=suffix;
        this.desc=desc;
    }
    public static FileType of(String suffix){
        for (FileType fileType : values()) {
            if(fileType.suffix.equals(suffix)){
                return fileType;
            }
        }
        return null;
    }
}
