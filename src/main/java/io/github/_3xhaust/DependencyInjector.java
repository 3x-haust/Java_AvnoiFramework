package io.github._3xhaust;

import java.util.Map;

public class DependencyInjector {
    public static void injectDependencies(Map<Class<?>, Object> applicationContext) throws IllegalAccessException {
        for (Object instance : applicationContext.values()) {
            for (java.lang.reflect.Field field : instance.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(io.github._3xhaust.annotations.Inject.class)) {
                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();

                    Object dependency = applicationContext.values().stream()
                            .filter(obj -> fieldType.isAssignableFrom(obj.getClass()))
                            .findFirst()
                            .orElse(null);

                    if (dependency != null) {
                        field.set(instance, dependency);
                    } else {
                        throw new IllegalArgumentException("Dependency not found for field: " + field.getName() +
                                " in class " + instance.getClass().getSimpleName() +
                                ". Required type: " + fieldType.getName() + ". Ensure all required dependencies are properly configured.");
                    }
                }
            }
        }
    }
}