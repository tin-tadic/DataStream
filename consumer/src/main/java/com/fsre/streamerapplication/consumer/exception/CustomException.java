package com.fsre.streamerapplication.consumer.exception;

public class CustomException extends RuntimeException {

    private static final long serialVersionUID = -7661881974219233311L;

    private final int code;

    public CustomException(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
