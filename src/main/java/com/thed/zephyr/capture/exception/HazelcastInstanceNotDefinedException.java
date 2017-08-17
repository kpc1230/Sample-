package com.thed.zephyr.capture.exception;

/**
 * Created by Masud on 8/16/17.
 */
public class HazelcastInstanceNotDefinedException extends Exception {

    public HazelcastInstanceNotDefinedException(String message) {
        super(message);
    }

    public HazelcastInstanceNotDefinedException(String message, Throwable cause) {
        super(message, cause);
    }

    public HazelcastInstanceNotDefinedException(Throwable cause) {
        super(cause);
    }

    public HazelcastInstanceNotDefinedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
