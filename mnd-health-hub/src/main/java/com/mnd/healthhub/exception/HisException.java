package com.mnd.healthhub.exception;

import lombok.Getter;

@Getter
public class HisException extends RuntimeException {

    // 基于JDK中的基础异常类扩展了一个code属性。
    private final int code;

    public HisException(String message) {
        this(message, 500);
    }

    public HisException(String message, int code) {
        super(message);
        this.code = code;
    }

    public HisException(String message, Throwable cause) {
        this(message, 500, cause);
    }

    public HisException(String message, int code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public HisException(Throwable cause) {
        this(cause.getMessage() != null ? cause.getMessage() : "系统异常", 500, cause);
    }
}