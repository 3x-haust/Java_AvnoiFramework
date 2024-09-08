package io.github._3xhaust;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

public class AvnoiHandler implements HttpHandler {
    private final Router router;
    private final ControllerDispatcher dispatcher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AvnoiHandler(Router router, ControllerDispatcher dispatcher) {
        this.router = router;
        this.dispatcher = dispatcher;
    }

    @Override
    public void handle(HttpExchange exchange) {
        CompletableFuture.runAsync(() -> {
            try {
                String method = exchange.getRequestMethod();
                String path = exchange.getRequestURI().getPath();

                Date today = new Date();
                String pattern = "MM/dd/yyyy, HH:mm:ss a";
                SimpleDateFormat df = new SimpleDateFormat(pattern);

                System.out.printf("[%s] %s %s\n", df.format(today), method, path);

                Method handler = router.findHandler(HttpMethod.valueOf(method), path);

                if (handler != null) {
                    Object result = dispatcher.dispatch(handler, exchange);
                    String responseBody = objectMapper.writeValueAsString(result);
                    sendResponse(exchange, 200, responseBody);
                } else {
                    sendResponse(exchange, 404, "Not Found");
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    sendResponse(exchange, 500, "Internal Server Error");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        exchange.sendResponseHeaders(statusCode, responseBody.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBody.getBytes());
        os.close();
    }
}