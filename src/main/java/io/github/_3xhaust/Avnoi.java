package io.github._3xhaust;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.github._3xhaust.annotations.Controller;
import io.github._3xhaust.annotations.Entity;
import io.github._3xhaust.annotations.Module;
import io.github._3xhaust.annotations.Service;
import io.github._3xhaust.orm.*;
import org.reflections.Reflections;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Avnoi {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static final Router router = new Router();
    private final ControllerDispatcher dispatcher;
    private static final Map<Class<?>, Object> applicationContext = new HashMap<>();
    private static int port = 8080;
    private static final String version = "0.1.0";

    public Avnoi(Class<?> modules) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        try {
            Class.forName(modules.getName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load AppModule", e);
        }

        scanAndInitialize(modules);
        this.dispatcher = new ControllerDispatcher(applicationContext);
    }

    public static void run(Class<?> modules) {
        try {
            Avnoi avnoi = new Avnoi(modules);
            avnoi.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void run() {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new AvnoiHandler());
            server.setExecutor(threadPoolExecutor);
            server.start();

            System.out.println("\n" + ANSI_BOLD + ANSI_PURPLE + "▲ Avnoi " + version + ANSI_RESET);
            System.out.println("- Local:              http://localhost:" + port);
            System.out.println();

        } catch (IOException e) {
            System.err.println("An issue occurred while starting the server: " + e.getMessage());
            System.err.println("Please check if port " + port + " is already in use.");
            System.err.println("To use a different port, call Avnoi.listen(portNumber).");
        }
    }

    public static void listen(int listeningPort) {
        Avnoi.port = listeningPort;
    }

    private void scanAndInitialize(Class<?> modules) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (modules.isAnnotationPresent(Module.class)) {
            Module moduleAnnotation = modules.getAnnotation(Module.class);

            for (Class<?> importedModule : moduleAnnotation.imports()) {
                scanAndInitialize(importedModule);
            }

            DataSourceOptions dataSourceOptions = AvnoiOrmModule.getDataSourceOptions();

            RepositoryFactory repositoryFactory = new RepositoryFactoryImpl(dataSourceOptions);
            applicationContext.put(RepositoryFactory.class, repositoryFactory);

            for (Class<?> providerClass : moduleAnnotation.providers()) {
                if (!providerClass.isAnnotationPresent(Service.class)) {
                    throw new IllegalArgumentException("Class must be annotated with @Service: " + providerClass.getName());
                }
                registerInstance(providerClass);
            }

            for (Class<?> controllerClass : moduleAnnotation.controllers()) {
                if (!controllerClass.isAnnotationPresent(Controller.class)) {
                    throw new IllegalArgumentException("Class must be annotated with @Controller: " + controllerClass.getName());
                }
                registerInstance(controllerClass);
                mapController(controllerClass);
            }
        }

        DependencyInjector.injectDependencies(applicationContext);
    }

    private void registerInstance(Class<?> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Object instance = createInstance(clazz);
        applicationContext.put(clazz, instance);
    }

    private Object createInstance(Class<?> clazz)
            throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(io.github._3xhaust.annotations.Inject.class)) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                Object[] parameters = new Object[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    parameters[i] = applicationContext.get(parameterTypes[i]);
                    if (parameters[i] == null) {
                        throw new RuntimeException("Dependency not found for " + parameterTypes[i].getName() +
                                ". Ensure all required dependencies are properly configured.");
                    }
                }
                return constructor.newInstance(parameters);
            }
        }

        return clazz.getDeclaredConstructor().newInstance();
    }

    private void mapController(Class<?> controller) {
        String baseUrl = controller.getAnnotation(Controller.class).value();

        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        for (Method method : controller.getDeclaredMethods()) {
            if (method.isAnnotationPresent(io.github._3xhaust.annotations.types.Get.class)) {
                String path = baseUrl + method.getAnnotation(io.github._3xhaust.annotations.types.Get.class).value();
                router.registerRoute(HttpMethod.GET, path, method);
            } else if (method.isAnnotationPresent(io.github._3xhaust.annotations.types.Post.class)) {
                String path = baseUrl + method.getAnnotation(io.github._3xhaust.annotations.types.Post.class).value();
                router.registerRoute(HttpMethod.POST, path, method);
            } else if (method.isAnnotationPresent(io.github._3xhaust.annotations.types.Put.class)) {
                String path = baseUrl + method.getAnnotation(io.github._3xhaust.annotations.types.Put.class).value();
                router.registerRoute(HttpMethod.PUT, path, method);
            } else if (method.isAnnotationPresent(io.github._3xhaust.annotations.types.Patch.class)) {
                String path = baseUrl + method.getAnnotation(io.github._3xhaust.annotations.types.Patch.class).value();
                router.registerRoute(HttpMethod.PATCH, path, method);
            } else if (method.isAnnotationPresent(io.github._3xhaust.annotations.types.Delete.class)) {
                String path = baseUrl + method.getAnnotation(io.github._3xhaust.annotations.types.Delete.class).value();
                router.registerRoute(HttpMethod.DELETE, path, method);
            }
        }
    }

    private class AvnoiHandler implements com.sun.net.httpserver.HttpHandler {
        private final ObjectMapper objectMapper;

        public AvnoiHandler() {
            objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            DefaultPrettyPrinter.Indenter indenter = new DefaultIndenter("  ", "\n");
            DefaultPrettyPrinter printer = new DefaultPrettyPrinter() {
                @Override
                public DefaultPrettyPrinter createInstance() {
                    return new DefaultPrettyPrinter(this);
                }

                @Override
                public void writeObjectFieldValueSeparator(JsonGenerator jg) throws IOException {
                    jg.writeRaw(": ");
                }
            };
            printer.indentObjectsWith(indenter);
            printer.indentArraysWith(indenter);

            objectMapper.setDefaultPrettyPrinter(printer);
        }

        @Override
        public void handle(HttpExchange exchange) {
            long startTime = System.currentTimeMillis();
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();
            CompletableFuture.runAsync(() -> {
                try {
                    int statusCode = 200;
                    String responseBody = "";

                    Method handler = router.findHandler(HttpMethod.valueOf(method), path);

                    if (handler != null) {
                        Object result = dispatcher.dispatch(handler, exchange);

                        if (result instanceof String) {
                            responseBody = (String) result;
                        } else {
                            responseBody = objectMapper.writeValueAsString(result);
                        }
                    } else {
                        statusCode = 404;
                        responseBody = "The requested page could not be found. Please check the URL and try again.";
                    }

                    sendResponse(exchange, statusCode, responseBody);

                    long endTime = System.currentTimeMillis();
                    long processingTime = endTime - startTime;
                    String statusColor = statusCode == 200 ? ANSI_GREEN : ANSI_YELLOW;

                    System.out.printf("[%s] %s %s %s%d%s in %dms\n",
                            ANSI_CYAN + LocalDateTime.now().format(dateTimeFormatter) + ANSI_RESET,
                            method,
                            path,
                            ANSI_BOLD + statusColor, statusCode, ANSI_RESET,
                            processingTime);
                } catch (Exception e) {
                    e.printStackTrace();
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

        private void sendResponse(HttpExchange exchange, int statusCode, String responseBody) throws IOException {
            exchange.sendResponseHeaders(statusCode, responseBody.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBody.getBytes());
            os.close();
        }
    }
}