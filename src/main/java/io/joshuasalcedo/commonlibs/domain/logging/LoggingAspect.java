package io.joshuasalcedo.commonlibs.domain.logging;

import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StopWatch;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * AOP aspect for method-level logging.
 */
@Aspect
public class LoggingAspect {
    private final LoggingFactory loggingFactory;
    private final LoggingProperties properties;

    public LoggingAspect(LoggingFactory loggingFactory, LoggingProperties properties) {
        this.loggingFactory = loggingFactory;
        this.properties = properties;
    }

    /**
     * Pointcut for all methods in the base package or subpackages.
     */
    @Pointcut("within(io.joshuasalcedo..*)")
    public void applicationPackagePointcut() {
        // Method is empty as this is just a pointcut definition
    }

    /**
     * Pointcut for methods annotated with @Logged annotation.
     */
    @Pointcut("@annotation(io.joshuasalcedo.commonlibs.domain.logging.Logged)")
    public void loggedAnnotationPointcut() {
        // Method is empty as this is just a pointcut definition
    }

    /**
     * Pointcut for all entity lifecycle event methods.
     */
    @Pointcut("execution(* io.joshuasalcedo.commonlibs.domain.listeners.EntityLifecycleListener.*(..))")
    public void entityLifecyclePointcut() {
        // Method is empty as this is just a pointcut definition
    }

    /**
     * Log method execution time and parameters for methods annotated with @Logged.
     */
    @Around("loggedAnnotationPointcut()")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        return logMethodExecutionInternal(joinPoint, true);
    }

    /**
     * Log entity lifecycle events if enabled.
     */
    @Around("entityLifecyclePointcut()")
    public Object logEntityLifecycle(ProceedingJoinPoint joinPoint) throws Throwable {
        // Check if entity lifecycle logging is enabled
        if (!properties.isEntityLifecycleLogging()) {
            return joinPoint.proceed();
        }
        return logMethodExecutionInternal(joinPoint, false);
    }

    /**
     * Internal method to log method execution details.
     */
    private Object logMethodExecutionInternal(ProceedingJoinPoint joinPoint, boolean detailed) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        LoggingService logger = loggingFactory.getLogger(joinPoint.getTarget().getClass());

        // Get method annotation for custom settings if present
        Logged annotation = method.getAnnotation(Logged.class);
        LogLevel level = annotation != null ? annotation.level() : LogLevel.DEBUG;

        StopWatch stopWatch = new StopWatch();
        try {
            if (detailed && logger.isDebugEnabled()) {
                String params = getMethodParams(joinPoint);
                log(logger, level, "Executing: {}#{} with params: [{}]",
                        joinPoint.getTarget().getClass().getSimpleName(),
                        method.getName(), params);
            }

            stopWatch.start();
            Object result = joinPoint.proceed();
            stopWatch.stop();

            if (detailed && logger.isDebugEnabled()) {
                log(logger, level, "Completed: {}#{} in {} ms",
                        joinPoint.getTarget().getClass().getSimpleName(),
                        method.getName(),
                        stopWatch.getTotalTimeMillis());
            }

            return result;
        } catch (Throwable e) {
            stopWatch.stop();

            log(logger, LogLevel.ERROR, "Exception in {}#{} after {} ms: {}",
                    joinPoint.getTarget().getClass().getSimpleName(),
                    method.getName(),
                    stopWatch.getTotalTimeMillis(),
                    e.getMessage());

            throw e;
        }
    }

    /**
     * Log exceptions thrown by methods in the application package.
     */
    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        LoggingService logger = loggingFactory.getLogger(joinPoint.getTarget().getClass());

        if (logger.isErrorEnabled()) {
            logger.error(
                    "Exception in {}#{}: {}",
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    e.getMessage(),
                    e
            );
        }
    }

    /**
     * Format method parameters for logging.
     */
    private String getMethodParams(JoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getArgs())
                .map(arg -> arg != null ? arg.toString() : "null")
                .collect(Collectors.joining(", "));
    }

    /**
     * Log a message with the appropriate log level.
     */
    private void log(LoggingService logger, LogLevel level, String message, Object... args) {
        switch (level) {
            case TRACE:
                logger.trace(message, args);
                break;
            case DEBUG:
                logger.debug(message, args);
                break;
            case INFO:
                logger.info(message, args);
                break;
            case WARN:
                logger.warn(message, args);
                break;
            case ERROR:
                logger.error(message, args);
                break;
        }
    }
}