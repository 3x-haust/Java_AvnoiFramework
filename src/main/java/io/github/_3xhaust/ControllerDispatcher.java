package io.github._3xhaust;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import io.github._3xhaust.annotations.types.Body;
import io.github._3xhaust.annotations.types.Param;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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

        System.out.println(Arrays.toString(parameters));

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
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        ContentType type = ContentType.fromString(contentType);

        if (type == null) {
            throw new IllegalArgumentException("Unsupported Content-Type: " + contentType);
        }

        return switch (type) {
            case JSON -> {
                ObjectMapper objectMapper = new ObjectMapper();
                yield objectMapper.readValue(exchange.getRequestBody(), parameter.getType());
            }
            case FORM_DATA -> {
                Map<String, Object> formData = parseFormData(exchange);
                Class<?> dtoClass = parameter.getType();
                yield mapToDto(formData, dtoClass);
            }
            case URL_ENCODED -> parseUrlEncodedData(exchange, parameter);
            case RAW -> readRawBody(exchange);
            case BINARY -> readBinaryBody(exchange);
            case GRAPHQL -> readRawBody(exchange);
            default -> throw new IllegalArgumentException("Unsupported Content-Type: " + contentType);
        };
    }

    private Object mapToDto(Map<String, Object> data, Class<?> dtoClass) {
        try {
            Object dtoInstance = dtoClass.getDeclaredConstructor().newInstance();
            for (java.lang.reflect.Field field : dtoClass.getDeclaredFields()) {
                String fieldName = field.getName();
                Object fieldValue = data.get(fieldName);

                if (fieldValue != null) {
                    field.setAccessible(true);
                    if (field.getType() == String.class && fieldValue instanceof byte[]) {
                        field.set(dtoInstance, new String((byte[]) fieldValue, StandardCharsets.UTF_8));
                    } else {
                        field.set(dtoInstance, convertStringToType(fieldValue.toString(), field.getType()));
                    }
                }
            }
            return dtoInstance;
        } catch (Exception e) {
            throw new RuntimeException("Error mapping data to DTO: " + e.getMessage(), e);
        }
    }

    private Map<String, Object> parseFormData(HttpExchange exchange) throws IOException {
        Map<String, Object> formData = new HashMap<>();
        String boundary = getBoundary(exchange.getRequestHeaders().getFirst("Content-Type"));

        if (boundary == null) {
            throw new IllegalArgumentException("Invalid Content-Type: multipart/form-data boundary not found");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFilePart = false;
            String fieldName = null;
            ByteArrayOutputStream fileContent = null;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("--" + boundary)) {
                    isFilePart = false;
                    if (fileContent != null) {
                        formData.put(fieldName, fileContent.toByteArray());
                        fileContent = null;
                    }
                } else if (line.startsWith("Content-Disposition: form-data;")) {
                    fieldName = extractFieldName(line);
                    String fileName = extractFileName(line);
                    if (fileName != null) {
                        isFilePart = true;
                        fileContent = new ByteArrayOutputStream();
                        formData.put(fieldName, fileName);
                    }
                } else if (line.isEmpty() && isFilePart) continue;
                else {
                    if (isFilePart) {
                        fileContent.write(line.getBytes(StandardCharsets.UTF_8));
                        fileContent.write("\n".getBytes(StandardCharsets.UTF_8));
                    } else formData.put(fieldName, line);
                }
            }

            if (fileContent != null) {
                formData.put(fieldName, fileContent.toByteArray());
            }
        }

        return formData;
    }

    private Object parseUrlEncodedData(HttpExchange exchange, Parameter parameter) throws IOException {
        String requestBody = readRawBody(exchange);
        Map<String, Object> urlEncodedData = new HashMap<>(); // Map<String, String>에서 변경
        String[] pairs = requestBody.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=");
            if (parts.length == 2) {
                urlEncodedData.put(parts[0], parts[1]);
            }
        }

        Class<?> dtoClass = parameter.getType();
        return mapToDto(urlEncodedData, dtoClass);
    }

    private String getBoundary(String contentType) {
        if (contentType == null) {
            return null;
        }

        String[] parts = contentType.split(";");
        for (String part : parts) {
            if (part.trim().startsWith("boundary=")) {
                return part.trim().substring("boundary=".length());
            }
        }
        return null;
    }

    private String extractFieldName(String line) {
        String[] parts = line.split(";");
        for (String part : parts) {
            if (part.trim().startsWith("name=\"")) {
                return part.trim().substring("name=\"".length(), part.trim().length() - 1);
            }
        }
        return null;
    }

    private String extractFileName(String line) {
        if (line.contains("filename=\"")) {
            int startIndex = line.indexOf("filename=\"") + "filename=\"".length();
            int endIndex = line.indexOf("\"", startIndex);
            return line.substring(startIndex, endIndex);
        }
        return null;
    }

    private Object convertStringToType(String value, Class<?> targetType) {
        if (targetType == String.class) {
            return value;
        } else if (targetType == int.class || targetType == Integer.class) {
            return Integer.parseInt(value);
        } else if (targetType == long.class || targetType == Long.class) {
            return Long.parseLong(value);
        } else if (targetType == boolean.class || targetType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (targetType == float.class || targetType == Float.class) {
            return Float.parseFloat(value);
        } else if (targetType == double.class || targetType == Double.class) {
            return Double.parseDouble(value);
        } else {
            throw new IllegalArgumentException("Unsupported field type: " + targetType.getName());
        }
    }
    private String readRawBody(HttpExchange exchange) throws IOException {
        InputStream inputStream = exchange.getRequestBody();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private byte[] readBinaryBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            return outputStream.toByteArray();
        }
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