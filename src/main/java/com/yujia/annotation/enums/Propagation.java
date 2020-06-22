package com.yujia.annotation.enums;

/**
 * 事务传播
 */
public enum Propagation {
    REQUIRED("REQUIRED"),
    SUPPORTS("SUPPORTS");

    private final String value;

    Propagation(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
