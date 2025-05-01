package io.joshuasalcedo.commonlibs.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for SSE Live Logs
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "io.joshuasalcedo.sse-live-logs")
public class SseLiveLogProperties {

    // Getters and setters
    /**
     * Whether SSE live logging is enabled
     */
    private boolean enabled = true;
    
    /**
     * Name of the root logger to attach the appender to
     */
    private String rootLoggerName = "ROOT";
    
    /**
     * Maximum number of logs to keep in memory
     */
    private int maxLogsInMemory = 1000;
    
    /**
     * Whether to send recent logs when a client connects
     */
    private boolean sendRecentLogsOnConnect = true;
    
    /**
     * SSE event timeout in milliseconds
     */
    private long timeoutMs = Long.MAX_VALUE;
    
    /**
     * Name of the SSE event
     */
    private String eventName = "log";
    
    /**
     * Pattern for formatting timestamps
     */
    private String timestampFormat = "hh:mm:ss a";
    
    /**
     * Whether to include exception information in log events
     */
    private boolean includeExceptions = true;
    
    /**
     * Whether to include stack traces in log events
     */
    private boolean includeStackTraces = true;

}