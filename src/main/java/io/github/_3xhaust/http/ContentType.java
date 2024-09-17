package io.github._3xhaust.http;

import lombok.Getter;

@Getter
public enum ContentType {
    FORM_DATA("multipart/form-data"),
    URL_ENCODED("application/x-www-form-urlencoded"),
    RAW("text/plain"),
    BINARY("application/octet-stream"),
    GRAPHQL("application/graphql"),
    JSON("application/json");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public static ContentType fromString(String value) {
        if (value == null) {
            return null;
        }

        String contentType = value.split(";")[0].trim();

        for (ContentType type : ContentType.values()) {
            if (type.getValue().equalsIgnoreCase(contentType)) {
                return type;
            }
        }
        return null;
    }
}