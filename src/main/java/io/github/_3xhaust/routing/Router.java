package io.github._3xhaust.routing;

import io.github._3xhaust.http.HttpMethod;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Router {
    private final TrieNode root = new TrieNode();

    public void registerRoute(HttpMethod method, String path, Method handler, int statusCode) {
        String[] parts = path.split("/");
        registerRouteRecursive(method, parts, 0, root, handler, statusCode);
    }

    private void registerRouteRecursive(HttpMethod method, String[] parts, int index, TrieNode node, Method handler, int statusCode) {
        if (index == parts.length) {
            node.getHandlers().put(method, new RouteHandler(handler, statusCode));
            return;
        }

        String part = parts[index];

        if (part.equals("*")) {
            node.getChildren().computeIfAbsent("*", k -> new TrieNode());
            registerRouteRecursive(method, parts, index + 1, node.getChildren().get("*"), handler, statusCode);
        } else {
            TrieNode nextNode = node.getChildren().computeIfAbsent(part, k -> new TrieNode());
            registerRouteRecursive(method, parts, index + 1, nextNode, handler, statusCode);
        }
    }

    public void registerRedirect(HttpMethod method, String path, String redirectPath, int statusCode) {
        String[] parts = path.split("/");
        TrieNode current = root;

        for (String part : parts) {
            if (part.isEmpty()) continue;
            current = current.getChildren().computeIfAbsent(part, k -> new TrieNode());
        }

        current.getRedirects().put(method, new Redirect(redirectPath, statusCode));
    }

    public RouteHandler findHandler(HttpMethod method, String path) {
        String[] parts = path.split("/");
        return findHandlerRecursive(method, parts, 0, root);
    }

    private RouteHandler findHandlerRecursive(HttpMethod method, String[] parts, int index, TrieNode node) {
        if (node == null) {
            return null;
        }

        if (index == parts.length) {
            return node.getHandlers().get(method);
        }

        String part = parts[index];

        for (Map.Entry<String, TrieNode> entry : node.getChildren().entrySet()) {
            String key = entry.getKey();
            TrieNode childNode = entry.getValue();

            if (key.equals("*") || part.matches(key.replace("*", ".*"))) {
                RouteHandler handler = findHandlerRecursive(method, parts, index + 1, childNode);
                if (handler != null) {
                    return handler;
                }
            }
        }

        return null;
    }

    Redirect findRedirect(HttpMethod method, String path) {
        String[] parts = path.split("/");
        TrieNode current = root;

        for (String part : parts) {
            if (part.isEmpty()) continue;

            current = current.getChildren().get(part);
            if (current == null) return null;
        }

        return current.getRedirects().get(method);
    }

    @Setter
    @Getter
    static class TrieNode {
        private final Map<String, TrieNode> children = new HashMap<>();
        private TrieNode wildcardChild;
        private final Map<HttpMethod, RouteHandler> handlers = new HashMap<>();
        private final Map<HttpMethod, Redirect> redirects = new HashMap<>();

    }
}