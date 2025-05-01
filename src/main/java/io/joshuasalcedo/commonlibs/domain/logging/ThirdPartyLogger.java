package io.joshuasalcedo.commonlibs.domain.logging;

import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

import java.util.Map;

/**
 * Configure logging levels for third-party libraries.
 * This allows controlling the verbosity of external dependencies.
 */
@Component
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.logging", name = "thirdPartyLogging", havingValue = "true", matchIfMissing = true)
public class ThirdPartyLogger {

    private final LoggingProperties properties;
    private final LoggingService logger;

    public ThirdPartyLogger(LoggingProperties properties, LoggingFactory loggingFactory) {
        this.properties = properties;
        this.logger = loggingFactory.getLogger(ThirdPartyLogger.class);
    }

    @PostConstruct
    public void init() {
        configureThirdPartyLoggers();
    }

    /**
     * Configure third-party loggers based on properties
     */
    private void configureThirdPartyLoggers() {
        if (properties.getThirdPartyLoggers() != null && !properties.getThirdPartyLoggers().isEmpty()) {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            
            for (Map.Entry<String, String> entry : properties.getThirdPartyLoggers().entrySet()) {
                String loggerName = entry.getKey();
                String levelName = entry.getValue();
                
                try {
                    Level level = Level.valueOf(levelName.toUpperCase());
                    Logger thirdPartyLogger = loggerContext.getLogger(loggerName);
                    thirdPartyLogger.setLevel(level);
                    
                    logger.info("Set logging level for {} to {}", loggerName, level);
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid logging level '{}' for logger '{}'", levelName, loggerName);
                }
            }
        }
    }

    public LoggingProperties getProperties() {
        return properties;
    }

    public LoggingService getLogger() {
        return logger;
    }
}