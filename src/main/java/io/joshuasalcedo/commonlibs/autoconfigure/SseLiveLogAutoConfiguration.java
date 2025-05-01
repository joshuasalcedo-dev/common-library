package io.joshuasalcedo.commonlibs.autoconfigure;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.joshuasalcedo.commonlibs.domain.base.controller.SseLiveLogsController;
import io.joshuasalcedo.commonlibs.properties.SseLiveLogProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Autoconfiguration for Server-Sent Events based live log streaming.
 * This approach is simpler than WebSockets and works with most modern browsers.
 * 
 * Can be enabled/disabled with the property: io.joshuasalcedo.sse-live-logs.enabled=true|false
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnProperty(name = "io.joshuasalcedo.sse-live-logs.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(SseLiveLogProperties.class)
public class SseLiveLogAutoConfiguration {

    private final ObjectMapper objectMapper;
    private final SseLiveLogProperties properties;
    private MemoryAppender memoryAppender;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final DateTimeFormatter formatter;

    public SseLiveLogAutoConfiguration(ObjectMapper objectMapper, SseLiveLogProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.formatter = DateTimeFormatter.ofPattern(properties.getTimestampFormat())
                .withZone(ZoneId.systemDefault());
    }

    @PostConstruct
    public void init() {
        memoryAppender = new MemoryAppender();
        memoryAppender.start();

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger(properties.getRootLoggerName());
        rootLogger.addAppender(memoryAppender);
    }

    @PreDestroy
    public void destroy() {
        if (memoryAppender != null) {
            memoryAppender.stop();
            
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger rootLogger = loggerContext.getLogger(properties.getRootLoggerName());
            rootLogger.detachAppender(memoryAppender);
        }
    }

    /**
     * Register a new SSE emitter for a client connection
     */
    public SseEmitter registerEmitter() {
        SseEmitter emitter = new SseEmitter(properties.getTimeoutMs());
        
        // Add the emitter to the list
        this.emitters.add(emitter);
        
        // Remove the emitter when the client disconnects
        emitter.onCompletion(() -> {
            this.emitters.remove(emitter);
        });
        
        emitter.onTimeout(() -> {
            emitter.complete();
            this.emitters.remove(emitter);
        });
        
        emitter.onError(e -> {
            emitter.complete();
            this.emitters.remove(emitter);
        });
        
        // Send recent logs on connect
        try {
            if (properties.isSendRecentLogsOnConnect()) {
                List<ILoggingEvent> recentLogs = memoryAppender.getRecentLogs();
                for (ILoggingEvent event : recentLogs) {
                    Map<String, Object> logData = formatLogEvent(event);
                    emitter.send(SseEmitter.event()
                            .name(properties.getEventName())
                            .data(objectMapper.writeValueAsString(logData)));
                }
            }
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
        
        return emitter;
    }

    /**
     * Format a log event into a map that can be sent to clients
     */
    private Map<String, Object> formatLogEvent(ILoggingEvent event) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("timestamp", formatter.format(Instant.ofEpochMilli(event.getTimeStamp())));
        logData.put("level", event.getLevel().toString());
        logData.put("thread", event.getThreadName());
        logData.put("logger", event.getLoggerName());
        logData.put("message", event.getFormattedMessage());
        
        // Include exception info if present
        if (event.getThrowableProxy() != null && properties.isIncludeExceptions()) {
            logData.put("exception", event.getThrowableProxy().getMessage());
            
            if (properties.isIncludeStackTraces()) {
                List<String> stackTrace = new ArrayList<>();
                for (ch.qos.logback.classic.spi.StackTraceElementProxy element : 
                        event.getThrowableProxy().getStackTraceElementProxyArray()) {
                    stackTrace.add(element.getStackTraceElement().toString());
                }
                logData.put("stackTrace", stackTrace);
            }
        }
        
        return logData;
    }

    /**
     * Custom Logback appender that keeps recent logs in memory and broadcasts to SSE clients
     */
    private class MemoryAppender extends AppenderBase<ILoggingEvent> {
        private final List<ILoggingEvent> logEvents = new ArrayList<>();

        @Override
        protected void append(ILoggingEvent event) {
            synchronized (logEvents) {
                // Add the event to memory
                logEvents.add(event);
                
                // Trim list if needed
                while (logEvents.size() > properties.getMaxLogsInMemory()) {
                    logEvents.remove(0);
                }
            }
            
            // Broadcast to all connected clients
            for (SseEmitter emitter : emitters) {
                try {
                    Map<String, Object> logData = formatLogEvent(event);
                    emitter.send(SseEmitter.event()
                            .name(properties.getEventName())
                            .data(objectMapper.writeValueAsString(logData)));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                } catch (Exception e) {
                    // Ignore other errors to prevent affecting the logging system
                }
            }
        }

        public List<ILoggingEvent> getRecentLogs() {
            synchronized (logEvents) {
                return new ArrayList<>(logEvents);
            }
        }
    }
    
    /**
     * Controller for accessing the live logs via SSE
     */
    @Bean
    @ConditionalOnMissingBean
    public SseLiveLogsController sseLiveLogsController() {
        return new SseLiveLogsController(this);
    }
}