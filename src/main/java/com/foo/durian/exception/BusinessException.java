package com.foo.durian.exception;

/**
 * 用于接口间错误信息提示; 一般地, 该类异常的message应该是用户可读的
 * 
 * version 1.0.0 Created by f on 16/8/17 下午4:21.
 */
public class BusinessException extends RuntimeException {

    public BusinessException() {
        super();
    }

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(Throwable cause) {
        super(cause);
    }

    public BusinessException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
