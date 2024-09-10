package io.github._3xhaust.annotations.orm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    String name() default "";
    String type() default "";
    int length() default 255;
    boolean unique() default false;
    boolean nullable() default true;
    String defaultValue() default "";
    String onUpdate() default "";
}

