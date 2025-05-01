package io.joshuasalcedo.commonlibs.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration properties for metrics and monitoring.
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "io.joshuasalcedo.common.metrics")
public class MetricsProperties {
    /**
     * Enable or disable metrics collection
     */
    private boolean enabled = true;

    /**
     * Enable JVM memory metrics
     */
    private boolean jvmMemoryMetrics = true;

    /**
     * Enable JVM garbage collection metrics
     */
    private boolean jvmGcMetrics = true;

    /**
     * Enable JVM thread metrics
     */
    private boolean jvmThreadMetrics = true;

    /**
     * Enable ClassLoader metrics
     */
    private boolean classLoaderMetrics = true;

    /**
     * Enable processor metrics
     */
    private boolean processorMetrics = true;

    /**
     * Common tags to apply to all metrics
     */
    private Map<String, String> commonTags = new HashMap<>();
}