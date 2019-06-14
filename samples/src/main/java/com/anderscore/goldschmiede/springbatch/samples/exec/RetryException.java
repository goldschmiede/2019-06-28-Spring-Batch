package com.anderscore.goldschmiede.springbatch.samples.exec;

public class RetryException extends RuntimeException {

    public RetryException(String message) {
        super(message);
    }
}
