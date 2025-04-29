package io.joshuasalcedo.commonlibs.domain.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be logged.
 * This annotation can be used on any method to enable detailed logging
 * of method entry, exit, parameters, return values, and execution time.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Logged {
    /**
     * The log level to use.
     */
    LogLevel level() default LogLevel.DEBUG;

    /**
     * Whether to log method parameters.
     */
    boolean logParameters() default true;

    /**
     * Whether to log the return value.
     */
    boolean logReturnValue() default false;

    /**
     * Whether to log execution time.
     */
    boolean logExecutionTime() default true;
}