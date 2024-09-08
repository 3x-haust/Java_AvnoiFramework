package io.github._3xhaust;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AvnoiHandler implements HttpHandler {
    private final Router router;
    private final ControllerDispatcher dispatcher;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Class<?>, Object> applicationContext;

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public AvnoiHandler(Router router, ControllerDispatcher dispatcher, Map<Class<?>, Object> applicationContext) {
        this.router = router;
        this.dispatcher = dispatcher;
        this.applicationContext = applicationContext;
    }

    @Override
    public void handle(HttpExchange exchange) {
        long startTime = System.currentTimeMillis();
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        CompletableFuture.runAsync(() -> {
            try {
                int statusCode = 200;

                Method handler = router.findHandler(HttpMethod.valueOf(method), path);

                if (handler != null) {
                    Object result = dispatcher.dispatch(handler, exchange);
                    String responseBody = objectMapper.writeValueAsString(result);
                    sendResponse(exchange, statusCode, responseBody);
                } else {
                    statusCode = 404;
                    sendResponse(exchange, statusCode, "The requested page could not be found. Please check the URL and try again.");
                }

                long endTime = System.currentTimeMillis();
                long processingTime = endTime - startTime;
                String statusColor = statusCode == 200 ? ANSI_GREEN : ANSI_YELLOW;

                System.out.printf("[%s] %s %s %s%d%s in %dms\n",
                        ANSI_CYAN + today() + ANSI_RESET,
                        method,
                        path,
                        ANSI_BOLD + statusColor, statusCode, ANSI_RESET,
                        processingTime);
            } catch (Exception e) {
                handleException(exchange, e, method, path, startTime);
            }
        });
    }

    private void handleException(HttpExchange exchange, Exception e, String method, String path, long startTime) {
        try {
            int statusCode = 500;
            String errorMessage = "An internal server error occurred. Please try again later.";
            if (e instanceof IllegalArgumentException) {
                statusCode = 400;
                errorMessage = "Invalid request: " + e.getMessage();
            }
            sendResponse(exchange, statusCode, errorMessage);

            long endTime = System.currentTimeMillis();
            long processingTime = endTime - startTime;
            System.out.printf("[%s] %s %s %s%d%s in %dms - Error: %s\n",
                    ANSI_CYAN + LocalDateTime.now().format(dateTimeFormatter) + ANSI_RESET,
                    method,
                    path,
                    ANSI_BOLD + ANSI_RED, statusCode, ANSI_RESET,
                    processingTime,
                    e.getMessage());
        } catch (IOException ex) {
            System.err.println("Failed to send error response: " + ex.getMessage());
        }
    }

    private String today() {
        Date today = new Date();
        String pattern = "MM/dd/yyyy, HH:mm:ss a";
        SimpleDateFormat df = new SimpleDateFormat(pattern);
        return df.format(today);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
        exchange.sendResponseHeaders(statusCode, responseBody.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBody.getBytes());
        os.close();
    }
}