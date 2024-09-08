package io.github._3xhaust.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Module {
    Class<?>[] controllers() default {};
    Class<?>[] providers() default {};
    Class<?>[] imports() default {};
}