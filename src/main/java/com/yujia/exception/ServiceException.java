package com.yujia.exception;

public class ServiceException extends RuntimeException {
    private int code;
    private String msg;

    public ServiceException() {
        super();
    }

    public ServiceException(String message) {
        super(message);
        this.msg = message;
    }

    public ServiceException(String message, int code, String msg) {
        super(message);
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ServiceException{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
