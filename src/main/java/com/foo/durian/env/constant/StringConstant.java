package com.foo.durian.env.constant;

import java.nio.charset.Charset;

/**
 * version 1.0.0
 * Created by foolish on 16/9/3 上午10:20.
 */
@Deprecated
public class StringConstant {
    public static final Charset utf8 = Charset.forName("utf-8");

    public static final String javaFileSuffix = ".java";

    public static final String xmlFileSuffix=".xml";

    public static final String newLine = "\n";

    public static final String doubleNewLine=newLine+newLine;

    public static final String blank = " ";


    public static final String braceBegin = "{";

    public static final String braceEnd = "}";

    public static final String lineEnd = ";";

    public static final String quote="\"";

    public static final String underLine="_";



}
