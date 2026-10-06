package com.barpro.notification.error;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(int status, String error, String message) {

    public static ApiError of(int status, String error, String message) {
        return new ApiError(status, error, message);
    }
}
