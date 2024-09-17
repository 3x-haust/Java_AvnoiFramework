package io.github._3xhaust;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import io.github._3xhaust.annotations.Query;
import io.github._3xhaust.annotations.http.Header;
import io.github._3xhaust.annotations.types.Body;
import io.github._3xhaust.annotations.http.Param;
import io.github._3xhaust.http.ContentType;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ControllerDispatcher {
    private final Map<Class<?>, Object> applicationContext;

    public ControllerDispatcher(Map<Class<?>, Object> applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Object dispatch(Method method, HttpExchange exchange) throws Exception {
        Object controllerInstance = applicationContext.get(method.getDeclaringClass());
        if (controllerInstance == null) {
            throw new IllegalStateException("Controller instance not found in applicationContext: " + method.getDeclaringClass().getName());
        }

        Header[] methodHeaders = method.getAnnotationsByType(Header.class);
        for (Header methodHeader : methodHeaders) {
            exchange.getResponseHeaders().add(methodHeader.value(), methodHeader.defaultValue());
        }

        if (method.isAnnotationPresent(io.github._3xhaust.annotations.Redirect.class)) {
            io.github._3xhaust.annotations.Redirect redirectAnnotation = method.getAnnotation(io.github._3xhaust.annotations.Redirect.class);

            Object result = method.invoke(controllerInstance, getMethodParameters(method, exchange));
            if (result instanceof Map && ((Map<?, ?>) result).containsKey("url")) {
                return result;
            } else {
                return Map.of("url", redirectAnnotation.url());
            }
        } else {
            Object[] parameters = getMethodParameters(method, exchange);
            Object result = method.invoke(controllerInstance, parameters);

            if (result instanceof CompletableFuture) {
                return result;
            } else {
                return result;
            }
        }
    }

    private Object[] getMethodParameters(Method method, HttpExchange exchange) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] values = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            if (parameter.isAnnotationPresent(Param.class)) {
                values[i] = getPathParameterValue(exchange, parameter);
            } else if (parameter.isAnnotationPresent(Query.class)) {
                values[i] = getQueryParameterValue(exchange, parameter);
            } else if (parameter.isAnnotationPresent(Body.class)) {
                values[i] = getRequestBody(exchange, parameter);
            }
        }

        return values;
    }

    private Object getPathParameterValue(HttpExchange exchange, Parameter parameter) {
        String paramName = parameter.getAnnotation(Param.class).value();
        String path = exchange.getRequestURI().getPath();

        Pattern pattern = Pattern.compile("/" + paramName + "/([^/]+)");
        Matcher matcher = pattern.matcher(path);
        if (matcher.find()) {
            String paramValue = matcher.group(1);
            return convertStringToType(paramValue, parameter.getType());
        }
        return null;
    }

    private Object getQueryParameterValue(HttpExchange exchange, Parameter parameter) throws Exception {
        Class<?> parameterType = parameter.getType();
        String queryString = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = parseQueryParams(queryString);

        Object dtoInstance = parameterType.getDeclaredConstructor().newInstance();
        for (Field field : parameterType.getDeclaredFields()) {
            String fieldName = field.getName();
            if (queryParams.containsKey(fieldName)) {
                String fieldValue = queryParams.get(fieldName);
                field.setAccessible(true);
                field.set(dtoInstance, convertStringToType(fieldValue, field.getType()));
            }
        }

        return dtoInstance;
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
            case RAW, GRAPHQL -> readRawBody(exchange);
            case BINARY -> readBinaryBody(exchange);
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
        Map<String, Object> urlEncodedData = new HashMap<>();
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