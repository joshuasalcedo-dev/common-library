package io.joshuasalcedo.commonlibs.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the logging system.
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "io.joshuasalcedo.common.logging")
public class LoggingProperties {
    /**
     * Enable or disable the custom logging
     */
    private boolean enabled = true;

    /**
     * Logging level for the application
     * Valid values: TRACE, DEBUG, INFO, WARN, ERROR
     */
    private String level = "INFO";

    /**
     * Enable or disable logging for entity lifecycle events
     */
    private boolean entityLifecycleLogging = true;

    /**
     * Enable or disable HTTP request logging
     */
    private boolean requestLogging = true;

    /**
     * Enable or disable log masking for sensitive data
     */
    private boolean maskSensitiveData = true;

    /**
     * Enable or disable SQL query logging
     */
    private boolean sqlLogging = true;

    /**
     * Pattern to use for logging
     */
    private String pattern = "${APP_NAME} - %d{HH:mm:ss.SSS} %highlight(%-5level) %magenta([%thread]) %boldCyan(%-40.40logger{39}) : %msg%n";
    
    /**
     * Package name to apply logging to (defaults to your base package)
     */
    private String basePackage = "io.joshuasalcedo";
}