package com.fsre.streamerapplication.consumer.exception;

public class CustomInvalidRequest extends CustomException {

    public CustomInvalidRequest(String message, int statusCode) {
        super(message, statusCode);
    }
}
