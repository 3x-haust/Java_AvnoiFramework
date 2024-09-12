package io.github._3xhaust;

import lombok.Getter;
import java.util.Map;

@Getter
public class HttpException extends RuntimeException {
    private final HttpStatus status;
    private final Map<String, Object> details;

    public HttpException(Map<String, Object> details, HttpStatus status) {
        this.details = details;
        this.status = status;
    }
}