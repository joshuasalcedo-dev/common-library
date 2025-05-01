package io.joshuasalcedo.commonlibs.domain.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;

import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * Configures Logback programmatically based on properties.
 * This is useful for adding file appenders or customizing logging behavior.
 */
@Component
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LoggingConfigurator {
    private final LoggingProperties properties;
    LoggingService logger;

    @Value("${logging.file.path:${user.home}/.app/logs}")
    private String logFilePath;

    @Value("${spring.application.name:application}")
    private String applicationName;


    public LoggingConfigurator(LoggingProperties properties, LoggingService logger) {
        this.properties = properties;
        this.logger = logger;
    }

    @PostConstruct
    public void configureLogback() {
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

            // Configure root logger level
            Logger rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
            rootLogger.setLevel(Level.toLevel(properties.getLevel(), Level.INFO));

            // Configure colorful console appender
            configureConsoleAppender(loggerContext, rootLogger);

            // Configure file appender
            configureFileAppender(loggerContext, rootLogger);

            // Configure base package logger
            if (properties.getBasePackage() != null && !properties.getBasePackage().isEmpty()) {
                Logger basePackageLogger = loggerContext.getLogger(properties.getBasePackage());
                basePackageLogger.setLevel(Level.toLevel(properties.getLevel(), Level.INFO));
            }

            logger.info("Logging configured with level: {}, base package: {}, log file: {}/{}.log",
                    properties.getLevel(), properties.getBasePackage(), logFilePath, applicationName);

        } catch (Exception e) {
            System.err.println("Error configuring logging: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configure a colorful console appender.
     */
    private void configureConsoleAppender(LoggerContext context, Logger rootLogger) {
        // Create the encoder for the appender
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("${APP_NAME} - %d{HH:mm:ss.SSS} %highlight(%-5level) %magenta([%thread]) %boldCyan(%-40.40logger{39}) : %msg%n");
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.start();

        // Create the console appender
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        appender.setName("CONSOLE");
        appender.setEncoder(encoder);
        appender.start();

        // Add the appender to the root logger
        rootLogger.addAppender(appender);
    }

    /**
     * Configure a rolling file appender for log files.
     */
    private void configureFileAppender(LoggerContext context, Logger rootLogger) {
        // Ensure log files exists
        File logDir = new File(logFilePath);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        // Create the encoder for the appender
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern("${APP_NAME} - %d{HH:mm:ss.SSS} %highlight(%-5level) %magenta([%thread]) %boldCyan(%-40.40logger{39}) : %msg%n");
        encoder.setCharset(StandardCharsets.UTF_8);
        encoder.start();

        // Create the rolling file appender
        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName("FILE");
        appender.setFile(logFilePath + "/" + applicationName + ".log");
        appender.setEncoder(encoder);
        appender.setAppend(true);

        // Create and set the rolling policy
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> policy = new SizeAndTimeBasedRollingPolicy<>();
        policy.setContext(context);
        policy.setParent(appender);
        policy.setFileNamePattern(logFilePath + "/" + applicationName + "/" + applicationName +"-%d{yyyy-MM-dd}-%i.log.gz");
        policy.setMaxFileSize(FileSize.valueOf("10MB"));
        policy.setMaxHistory(30);
        policy.setTotalSizeCap(FileSize.valueOf("3GB"));
        policy.start();

        appender.setRollingPolicy(policy);
        appender.start();

        // Add the appender to the root logger
        rootLogger.addAppender(appender);
    }
}