package io.github._3xhaust;

import io.github._3xhaust.annotations.Inject;

import java.util.Map;

public class DependencyInjector {
    public static void injectDependencies(Map<Class<?>, Object> applicationContext) throws IllegalAccessException {
        for (Object instance : applicationContext.values()) {
            for (java.lang.reflect.Field field : instance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Inject.class)) {
                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();

                    Object dependency = applicationContext.values().stream()
                            .filter(obj -> fieldType.isAssignableFrom(obj.getClass()))
                            .findFirst()
                            .orElse(null);

                    if (dependency != null) {
                        field.set(instance, dependency);
                    } else {
                        System.err.println("Dependency not found for: " + fieldType.getName());
                    }
                }
            }
        }
    }
}