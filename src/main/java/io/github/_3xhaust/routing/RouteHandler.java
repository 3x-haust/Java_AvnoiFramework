package io.github._3xhaust.routing;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

@Getter
@RequiredArgsConstructor
public class RouteHandler {
    private final Method handlerMethod;
    private final int statusCode;
}