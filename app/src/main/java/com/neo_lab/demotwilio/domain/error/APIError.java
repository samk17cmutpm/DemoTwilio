package com.neo_lab.demotwilio.domain.error;

/**
 * Created by sam_nguyen on 12/04/2017.
 */

public class APIError {
    private int statusCode;
    private String message;

    public APIError() {
    }

    public int status() {
        return statusCode;
    }

    public String message() {
        return message;
    }
}
