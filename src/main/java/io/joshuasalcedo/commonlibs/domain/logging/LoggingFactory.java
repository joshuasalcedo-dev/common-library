package io.joshuasalcedo.commonlibs.domain.logging;

import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoggingFactory {
    private final LoggingProperties properties;
    private final Map<String, LoggingService> loggerCache = new ConcurrentHashMap<>();

    public LoggingFactory(LoggingProperties properties) {
        this.properties = properties;
    }

    public LoggingService getLogger(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Logger class cannot be null");
        }
        return loggerCache.computeIfAbsent(
                clazz.getName(),
                name -> new LoggingService(clazz, properties)
        );
    }

    public LoggingService getLogger(String name) {
        return loggerCache.computeIfAbsent(
                name,
                n -> new LoggingService(name, properties)
        );
    }

    public void clearCache() {
        loggerCache.clear();
    }
}