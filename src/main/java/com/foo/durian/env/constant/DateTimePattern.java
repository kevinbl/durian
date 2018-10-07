package com.foo.durian.env.constant;

/**
 * Created by foolish on 2017/2/14.
 */
public enum DateTimePattern {

    DEFAULT("yyyy-MM-dd HH:mm:ss"), SLASHED("yyyy/MM/dd HH:mm:ss"), COMPACT("yyyyMMddHHmmss");

    public final String pattern;

    private static final int FULL_LENGTH = 19;

    DateTimePattern(String pattern) {
        this.pattern = pattern;
    }

    public String datetime() {
        return pattern;
    }

    public String date() {
        return isInFullLength() ? pattern.substring(0, 10) : pattern.substring(0, 8);
    }

    public String month() {
        return isInFullLength() ? pattern.substring(0, 7) : pattern.substring(0, 6);
    }

    public String year() {
        return pattern.substring(0, 4);
    }

    private boolean isInFullLength() {
        return pattern.length() == FULL_LENGTH;
    }
}
