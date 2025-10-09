package com.evilcorp.api.exception;

public class ApiException extends RuntimeException {
    public ApiException(String s) {
        super(s);
    }

    public ApiException(String s, Object... objects) {
        super(String.format(s, objects));
    }
}
