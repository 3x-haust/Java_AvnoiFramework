package io.github._3xhaust.routing;

import lombok.Getter;

@Getter
class Redirect {
    private final String path;
    private final int statusCode;

    public Redirect(String path, int statusCode) {
        this.path = path;
        this.statusCode = statusCode;
    }
}
