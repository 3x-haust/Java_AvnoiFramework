package io.github._3xhaust;

import com.sun.net.httpserver.HttpServer;
import io.github._3xhaust.annotations.Controller;
import io.github._3xhaust.annotations.types.*;
import org.reflections.Reflections;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Avnoi {
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_PURPLE = "\u001B[35m";

    private static final Router router = new Router();
    private final ControllerDispatcher dispatcher;
    private static final Map<Class<?>, Object> applicationContext = new HashMap<>();
    private static int port = 8080;
    private static final String version = "0.1.0";

    public Avnoi(Class<?> mainClass, Class<?>... modules) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        scanAndInitialize(mainClass, modules);
        this.dispatcher = new ControllerDispatcher(applicationContext);
    }

    public static void run(Class<?> mainClass, Class<?>... modules) {
        try {
            Avnoi avnoi = new Avnoi(mainClass, modules);
            avnoi.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void run() {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new AvnoiHandler(router, dispatcher));
            server.setExecutor(threadPoolExecutor);
            server.start();

            System.out.println("\n" + ANSI_BOLD + ANSI_PURPLE + "▲ Avnoi " + version + ANSI_RESET);
            System.out.println("- Local:              http://localhost:" + port);
            System.out.println();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void listen(int listeningPort) {
        Avnoi.port = listeningPort;
    }

    private static void scanAndInitialize(Class<?> mainClass, Class<?>... modules) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        AvnoiScanner.scanAndInitialize(mainClass, applicationContext, modules);
        mapControllers(mainClass);
    }

    private static void mapControllers(Class<?> mainClass) {
        Reflections reflections = new Reflections(mainClass.getPackageName());
        Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);

        for (Class<?> controller : controllers) {
            String baseUrl = controller.getAnnotation(Controller.class).value();

            if (!baseUrl.endsWith("/")) {
                baseUrl += "/";
            }

            for (Method method : controller.getDeclaredMethods()) {
                if (method.isAnnotationPresent(Get.class)) {
                    String path = baseUrl + method.getAnnotation(Get.class).value();
                    router.registerRoute(HttpMethod.GET, path, method);
                } else if (method.isAnnotationPresent(Post.class)) {
                    String path = baseUrl + method.getAnnotation(Post.class).value();
                    router.registerRoute(HttpMethod.POST, path, method);
                } else if (method.isAnnotationPresent(Put.class)) {
                    String path = baseUrl + method.getAnnotation(Put.class).value();
                    router.registerRoute(HttpMethod.PUT, path, method);
                } else if (method.isAnnotationPresent(Patch.class)) {
                    String path = baseUrl + method.getAnnotation(Patch.class).value();
                    router.registerRoute(HttpMethod.PATCH, path, method);
                } else if (method.isAnnotationPresent(Delete.class)) {
                    String path = baseUrl + method.getAnnotation(Delete.class).value();
                    router.registerRoute(HttpMethod.DELETE, path, method);
                }
            }
        }
    }
}
