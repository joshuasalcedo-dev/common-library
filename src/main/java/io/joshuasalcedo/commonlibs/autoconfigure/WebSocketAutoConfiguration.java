package io.joshuasalcedo.commonlibs.autoconfigure;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Auto-configuration for WebSocket support.
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnClass(WebSocketMessageBrokerConfigurer.class)
@ConditionalOnProperty(prefix = "app.live-logs", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WebSocketAutoConfiguration implements WebSocketMessageBrokerConfigurer {

    private SimpMessagingTemplate messagingTemplate;
    private WebSocketLogAppender logAppender;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider;

    @Value("${app.live-logs.enabled:true}")
    private boolean liveLogsEnabled;

    @Value("${app.live-logs.level:INFO}")
    private String liveLogsLevel;

    public WebSocketAutoConfiguration(ObjectMapper objectMapper,
                                  ObjectProvider<SimpMessagingTemplate> messagingTemplateProvider) {
        this.objectMapper = objectMapper;
        this.messagingTemplateProvider = messagingTemplateProvider;
    }



    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-logs")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    @PostConstruct
    public void init() {
        if (liveLogsEnabled) {
            // Defer getting the messaging template until it's available
            // This breaks the circular dependency
            messagingTemplate = messagingTemplateProvider.getIfAvailable();

            if (messagingTemplate != null) {
                logAppender = new WebSocketLogAppender(messagingTemplate, objectMapper);
                logAppender.start();

                LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
                Logger rootLogger = loggerContext.getLogger("ROOT");
                rootLogger.addAppender(logAppender);
            }
        }
    }

    @PreDestroy
    public void destroy() {
        if (logAppender != null) {
            logAppender.stop();

            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger rootLogger = loggerContext.getLogger("ROOT");
            rootLogger.detachAppender(logAppender);
        }
    }

    /**
     * Custom Logback appender that publishes log events to a WebSocket topic.
     */
    public static class WebSocketLogAppender extends AppenderBase<ILoggingEvent> {
        private final SimpMessagingTemplate messagingTemplate;
        private final ObjectMapper objectMapper;
        private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a")
                .withZone(ZoneId.systemDefault());

        public WebSocketLogAppender(SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
            this.messagingTemplate = messagingTemplate;
            this.objectMapper = objectMapper;
        }

        @Override
        protected void append(ILoggingEvent event) {
            try {
                Map<String, Object> logData = new HashMap<>();
                logData.put("timestamp", formatter.format(Instant.ofEpochMilli(event.getTimeStamp())));
                logData.put("level", event.getLevel().toString());
                logData.put("thread", event.getThreadName());
                logData.put("logger", event.getLoggerName());
                logData.put("message", event.getFormattedMessage());

                // Include exception info if present
                if (event.getThrowableProxy() != null) {
                    logData.put("exception", event.getThrowableProxy().getMessage());
                    logData.put("stackTrace", event.getThrowableProxy().getStackTraceElementProxyArray());
                }

                messagingTemplate.convertAndSend("/topic/logs", objectMapper.writeValueAsString(logData));
            } catch (JsonProcessingException e) {
                // Don't log the error here to avoid potential infinite loop
                System.err.println("Error processing log event for WebSocket: " + e.getMessage());
            }
        }
    }

    // Remove the nested configuration class since it's creating duplicate beans
    // The main WebSocketLiveLogConfig will be auto-detected through component scanning
}