package com.foo.durian.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;

/**
 * Version 1.0.0
 * Created by f on 16/8/18.
 */
public class EncodingUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(EncodingUtil.class);

    private static final String DEFAULT_ENCODING = "utf-8";

    /**
     * 按照默认编码方式将字符串编码成对应的latin1字符串
     *
     * @param string 待编码的字符串
     * @return 从编码后得到的字节数组转成的latin1字符串
     */
    public static String encodeToLatin1String(String string) {
        return encodeToLatin1String(string, null);
    }

    /**
     * 按照指定的编码方式将字符串编码成对应的latin1字符串
     *
     * @param string 待编码的字符串
     * @param encoding 编/解码方式
     * @return 从编码后得到的字节数组转成的latin1字符串
     */
    public static String encodeToLatin1String(String string, String encoding){
        if (string == null) throw new IllegalArgumentException("字符串为NULL");

        try {
            byte[] bytes = string.getBytes(encoding != null ? encoding : DEFAULT_ENCODING);
            return new String(bytes, "latin1");     // 此处对于编码后的字节数组以latin1的解码成字符串, 用于以字符串的形式存储/传输字节数组
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("不支持的编/解码方式: {}", e.getMessage(), e);
            throw new RuntimeException("不支持的编/解码方式: " + encoding);
        }
    }

    /**
     * 按照默认解码方式将latin1字符串解码成对应的字符串
     *
     * @param string 待解码的字符串
     * @return 从latin1解码后得到的字节数组转成的字符串
     */
    public static String decodeFromLatin1String(String string) {
        return decodeFromLatin1String(string, null);
    }

    /**
     * 按照指定解码方式将latin1字符串解码成对应的字符串
     *
     * @param string 待解码的字符串
     * @param encoding 编/解码方式
     * @return 从latin1解码后得到的字节数组转成的字符串
     */
    public static String decodeFromLatin1String(String string, String encoding){
        if (string == null) throw new IllegalArgumentException("字符串为NULL");

        try {
            byte[] bytes = string.getBytes("latin1");   // new String(bytes, "latin1")的逆过程!
            return new String(bytes, encoding != null ? encoding : DEFAULT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("不支持的编/解码方式: {}", e.getMessage(), e);
            throw new RuntimeException("不支持的编/解码方式: " + encoding);
        }
    }
}
