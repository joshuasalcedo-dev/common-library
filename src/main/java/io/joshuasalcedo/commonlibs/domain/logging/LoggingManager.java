package io.joshuasalcedo.commonlibs.domain.logging;

/**
 * Static utility class for accessing loggers throughout the application.
 * This provides a convenient way to get a logger without directly 
 * accessing the Spring context.
 */
public final class LoggingManager {
    private static LoggingFactory loggingFactory;
    


    public static void setLoggingFactory(LoggingFactory factory) {
        loggingFactory = factory;
    }

    public static LoggingService getLogger(Class<?> clazz) {
        if (loggingFactory == null) {
            throw new IllegalStateException("LoggingFactory not initialized");
        }
        return loggingFactory.getLogger(clazz);
    }

    public static LoggingService getLogger(String name) {
        if (loggingFactory == null) {
            throw new IllegalStateException("LoggingFactory not initialized");
        }
        return loggingFactory.getLogger(name);
    }

}