package com.bjdx.rice.business.exception;

public class MyStreamException extends MyException{

    public MyStreamException(int code , String message, Throwable e) {
        super(code, message,e);  // 固定 code 为 500
    }

    public MyStreamException(int code, String message) {
        super(code, message);  // 固定 code 为 500
    }

    public MyStreamException(String message) {
        super(2000, message);  // 固定 code 为 500
    }
}
