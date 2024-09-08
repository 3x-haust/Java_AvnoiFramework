package io.github._3xhaust;

import lombok.Getter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Router {
    private final TrieNode root = new TrieNode();

    public void registerRoute(HttpMethod method, String path, Method handler) {
        String[] parts = path.split("/");
        TrieNode current = root;

        for (String part : parts) {
            if (part.isEmpty()) continue;
            current = current.getChildren().computeIfAbsent(part, k -> new TrieNode());
        }

        current.getHandlers().put(method, handler);
    }

    public Method findHandler(HttpMethod method, String path) {
        String[] parts = path.split("/");
        TrieNode current = root;

        for (String part : parts) {
            if (part.isEmpty()) continue;

            current = current.getChildren().get(part);
            if (current == null) return null;
        }

        return current.getHandlers().get(method);
    }

    @Getter
    static class TrieNode {
        private final Map<String, TrieNode> children = new HashMap<>();
        private final Map<HttpMethod, Method> handlers = new HashMap<>();
    }
}