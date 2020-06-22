package com.yujia.exception;

public class ProxyException extends ServiceException {
    public ProxyException(String message) {
        super(message);
    }

    public static ProxyException build (String message) {
        return new ProxyException(message);
    }
}
