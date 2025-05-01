package io.joshuasalcedo.commonlibs.config;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.joshuasalcedo.commonlibs.controller.SseLiveLogsController;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
 * Configuration for Server-Sent Events based live log streaming.
 * This approach is simpler than WebSockets and works with most modern browsers.
 */
@Configuration
public class SseLiveLogConfig {

    private final ObjectMapper objectMapper;
    private MemoryAppender memoryAppender;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a")
            .withZone(ZoneId.systemDefault());

    public SseLiveLogConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        memoryAppender = new MemoryAppender();
        memoryAppender.start();

        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger rootLogger = loggerContext.getLogger("ROOT");
        rootLogger.addAppender(memoryAppender);
    }

    @PreDestroy
    public void destroy() {
        if (memoryAppender != null) {
            memoryAppender.stop();

            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            Logger rootLogger = loggerContext.getLogger("ROOT");
            rootLogger.detachAppender(memoryAppender);
        }
    }

    /**
     * Register a new SSE emitter for a client connection
     */
    public SseEmitter registerEmitter() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

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
            List<ILoggingEvent> recentLogs = memoryAppender.getRecentLogs();
            for (ILoggingEvent event : recentLogs) {
                Map<String, Object> logData = formatLogEvent(event);
                emitter.send(SseEmitter.event()
                        .name("log")
                        .data(objectMapper.writeValueAsString(logData)));
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
        if (event.getThrowableProxy() != null) {
            logData.put("exception", event.getThrowableProxy().getMessage());

            List<String> stackTrace = new ArrayList<>();
            for (ch.qos.logback.classic.spi.StackTraceElementProxy element : event.getThrowableProxy().getStackTraceElementProxyArray()) {
                stackTrace.add(element.getStackTraceElement().toString());
            }
            logData.put("stackTrace", stackTrace);
        }

        return logData;
    }

    /**
     * Custom Logback appender that keeps recent logs in memory and broadcasts to SSE clients
     */
    private class MemoryAppender extends AppenderBase<ILoggingEvent> {
        private final int MAX_LOGS = 1000;
        private final List<ILoggingEvent> logEvents = new ArrayList<>();

        @Override
        protected void append(ILoggingEvent event) {
            synchronized (logEvents) {
                // Add the event to memory
                logEvents.add(event);

                // Trim list if needed
                while (logEvents.size() > MAX_LOGS) {
                    logEvents.remove(0);
                }
            }

            // Broadcast to all connected clients
            for (SseEmitter emitter : emitters) {
                try {
                    Map<String, Object> logData = formatLogEvent(event);
                    emitter.send(SseEmitter.event()
                            .name("log")
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
    public SseLiveLogsController sseLiveLogsController() {
        return new SseLiveLogsController(this);
    }
}