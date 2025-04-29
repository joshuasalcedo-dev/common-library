package io.joshuasalcedo.commonlibs.domain.logging;

import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Core logging service that provides consistent logging functionality.
 */

public class LoggingService {
    private final LoggingProperties properties;
    private final Logger logger;

    // Patterns for sensitive data masking
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");
    private static final Pattern CREDIT_CARD_PATTERN =
            Pattern.compile("\\b(?:\\d[ -]*?){13,16}\\b");
    private static final Pattern SSN_PATTERN =
            Pattern.compile("\\b\\d{3}[-]?\\d{2}[-]?\\d{4}\\b");

    public LoggingService(Class<?> loggerClass, LoggingProperties properties) {
        this.properties = properties;
        this.logger = LoggerFactory.getLogger(loggerClass);
    }

    public LoggingService(String loggerName, LoggingProperties properties) {
        this.properties = properties;
        this.logger = LoggerFactory.getLogger(loggerName);
    }



    /**
     * Log a trace message.
     */
    public void trace(String message, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(maskSensitiveData(message), args);
        }
    }

    /**
     * Log a debug message.
     */
    public void debug(String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(maskSensitiveData(message), args);
        }
    }

    /**
     * Log an info message.
     */
    public void info(String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(maskSensitiveData(message), args);
        }
    }

    /**
     * Log a warning message.
     */
    public void warn(String message, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(maskSensitiveData(message), args);
        }
    }

    /**
     * Log a warning message with exception.
     */
    public void warn(String message, Throwable throwable) {
        if (logger.isWarnEnabled()) {
            logger.warn(maskSensitiveData(message), throwable);
        }
    }

    /**
     * Log an error message.
     */
    public void error(String message, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(maskSensitiveData(message), args);
        }
    }

    /**
     * Log an error message with exception.
     */
    public void error(String message, Throwable throwable) {
        if (logger.isErrorEnabled()) {
            logger.error(maskSensitiveData(message), throwable);
        }
    }

    /**
     * Log an error with an exception and additional context.
     */
    public void error(String message, Throwable throwable, Object... context) {
        if (logger.isErrorEnabled()) {
            String contextStr = StringUtils.hasText(message) ? message + " " : "";
            contextStr += "Context: " + Arrays.toString(context);
            logger.error(maskSensitiveData(contextStr), throwable);
        }
    }

    /**
     * Mask sensitive data in the log message if enabled.
     */
    private String maskSensitiveData(String message) {
        if (!properties.isMaskSensitiveData() || message == null) {
            return message;
        }

        // Mask email addresses
        message = EMAIL_PATTERN.matcher(message).replaceAll(match -> {
            String email = match.group();
            int atIndex = email.indexOf('@');
            if (atIndex > 1) {
                return email.charAt(0) + "****" + email.substring(atIndex);
            }
            return email;
        });

        // Mask credit card numbers
        message = CREDIT_CARD_PATTERN.matcher(message).replaceAll(match -> {
            String cc = match.group().replaceAll("[ -]", "");
            if (cc.length() >= 13) {
                return "****" + cc.substring(cc.length() - 4);
            }
            return cc;
        });

        // Mask SSNs
        message = SSN_PATTERN.matcher(message).replaceAll("***-**-****");

        return message;
    }

    /**
     * Check if the trace level is enabled.
     */
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    /**
     * Check if the debug level is enabled.
     */
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    /**
     * Check if the info level is enabled.
     */
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    /**
     * Check if the warn level is enabled.
     */
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    /**
     * Check if the error level is enabled.
     */
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }
}