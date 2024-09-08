package io.github._3xhaust;

import io.github._3xhaust.annotations.Controller;
import io.github._3xhaust.annotations.Module;
import io.github._3xhaust.annotations.Service;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AvnoiScanner {

    public static void scanAndInitialize(Class<?> mainClass, Map<Class<?>, Object> applicationContext, Class<?>... modules)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        List<Class<?>> allModules = new ArrayList<>(Arrays.asList(modules));
        allModules.add(mainClass);

        for (Class<?> moduleClass : allModules) {
            scanAndInitialize(moduleClass, applicationContext);
        }
    }

    private static void scanAndInitialize(Class<?> moduleClass, Map<Class<?>, Object> applicationContext)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (moduleClass.isAnnotationPresent(Module.class)) {
            Module moduleAnnotation = moduleClass.getAnnotation(Module.class);

            for (Class<?> providerClass : moduleAnnotation.providers()) {
                Object providerInstance = createInstance(providerClass, applicationContext);
                applicationContext.put(providerClass, providerInstance);
            }

            for (Class<?> controllerClass : moduleAnnotation.controllers()) {
                Object controllerInstance = createInstance(controllerClass, applicationContext);
                applicationContext.put(controllerClass, controllerInstance);
            }

            processModule(moduleAnnotation, applicationContext);

            for (Class<?> importedModule : moduleAnnotation.imports()) {
                scanAndInitialize(importedModule, applicationContext);
            }
        }
    }

    private static void processModule(Module moduleAnnotation, Map<Class<?>, Object> applicationContext) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        for (Class<?> controllerClass : moduleAnnotation.controllers()) {
            if (!controllerClass.isAnnotationPresent(Controller.class)) {
                throw new IllegalArgumentException("Class must be annotated with @Controller: " + controllerClass.getName());
            }
            Object controllerInstance = createInstance(controllerClass, applicationContext);
            applicationContext.put(controllerClass, controllerInstance);
        }

        for (Class<?> providerClass : moduleAnnotation.providers()) {
            if (!providerClass.isAnnotationPresent(Service.class)) {
                throw new IllegalArgumentException("Class must be annotated with @Service: " + providerClass.getName());
            }
            Object providerInstance = createInstance(providerClass, applicationContext);
            applicationContext.put(providerClass, providerInstance);
        }
    }

    private static Object createInstance(Class<?> clazz, Map<Class<?>, Object> applicationContext) throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Constructor<?>[] constructors = clazz.getConstructors();

        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(io.github._3xhaust.annotations.Inject.class) || constructor.getParameterCount() > 0) {
                Class<?>[] parameterTypes = constructor.getParameterTypes();

                Object[] parameters = Arrays.stream(parameterTypes)
                        .map(paramType -> {
                            Object dependency = applicationContext.get(paramType);
                            if (dependency == null) {
                                throw new RuntimeException("No dependency found for " + paramType.getName());
                            }
                            return dependency;
                        })
                        .toArray();

                return constructor.newInstance(parameters);
            }
        }

        return clazz.getDeclaredConstructor().newInstance();
    }
}