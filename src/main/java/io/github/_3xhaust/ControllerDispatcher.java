package io.github._3xhaust;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import io.github._3xhaust.annotations.types.Body;
import io.github._3xhaust.annotations.types.Param;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class ControllerDispatcher {
    private final Map<Class<?>, Object> controllers;

    public ControllerDispatcher(Map<Class<?>, Object> controllers) {
        this.controllers = controllers;
    }

    public Object dispatch(Method method, HttpExchange exchange) throws Exception {
        Object controllerInstance = controllers.get(method.getDeclaringClass());
        Object[] parameters = getMethodParameters(method, exchange);

        return method.invoke(controllerInstance, parameters);
    }

    private Object[] getMethodParameters(Method method, HttpExchange exchange) throws IOException {
        Parameter[] parameters = method.getParameters();
        Object[] values = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            if (parameter.isAnnotationPresent(Param.class)) {
                values[i] = getParameterValue(exchange, parameter);
            } else if (parameter.isAnnotationPresent(Body.class)) {
                values[i] = getRequestBody(exchange, parameter);
            }
        }

        return values;
    }

    private Object getParameterValue(HttpExchange exchange, Parameter parameter) {
        String paramName = parameter.getAnnotation(Param.class).value();
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = parseQueryParams(query);
        String paramValue = queryParams.get(paramName);

        if (paramValue == null) {
            return null;
        }

        Class<?> parameterType = parameter.getType();
        if (parameterType == int.class || parameterType == Integer.class) {
            return Integer.parseInt(paramValue);
        } else if (parameterType == String.class) {
            return paramValue;
        } else {
            throw new IllegalArgumentException("Unsupported parameter type: " + parameterType);
        }
    }

    private Object getRequestBody(HttpExchange exchange, Parameter parameter) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(exchange.getRequestBody(), parameter.getType());
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> queryParams = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] parts = param.split("=");
                if (parts.length == 2) {
                    queryParams.put(parts[0], parts[1]);
                } else if (parts.length == 1) {
                    queryParams.put(parts[0], "");
                }
            }
        }
        return queryParams;
    }
}