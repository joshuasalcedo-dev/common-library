package io.joshuasalcedo.commonlibs.autoconfigure;

import io.joshuasalcedo.commonlibs.properties.MetricsProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics;
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics;
import io.micrometer.core.instrument.binder.system.ProcessorMetrics;
import org.springframework.boot.actuate.autoconfigure.metrics.CompositeMeterRegistryAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.metrics.export.simple.SimpleMetricsExportAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Auto-configuration for Actuator with Prometheus metrics.
 * This configuration sets up JVM metrics, system metrics, and application custom metrics.
 */
@Configuration
@AutoConfiguration(after = {
        MetricsAutoConfiguration.class,
        CompositeMeterRegistryAutoConfiguration.class,
        SimpleMetricsExportAutoConfiguration.class
})
@EnableConfigurationProperties(MetricsProperties.class)
@ConditionalOnClass(MeterRegistry.class)
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.metrics", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ActuatorAutoConfiguration {

    private final MetricsProperties properties;

    public ActuatorAutoConfiguration(MetricsProperties properties) {
        this.properties = properties;
    }

    /**
     * Register JVM memory metrics (heap, non-heap, buffer pools)
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "io.joshuasalcedo.common.metrics", name = "jvmMemoryMetrics", havingValue = "true", matchIfMissing = true)
    public JvmMemoryMetrics jvmMemoryMetrics() {
        return new JvmMemoryMetrics();
    }

    /**
     * Register JVM garbage collection metrics
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "io.joshuasalcedo.common.metrics", name = "jvmGcMetrics", havingValue = "true", matchIfMissing = true)
    public JvmGcMetrics jvmGcMetrics() {
        return new JvmGcMetrics();
    }

    /**
     * Register JVM thread metrics (counts, states)
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "io.joshuasalcedo.common.metrics", name = "jvmThreadMetrics", havingValue = "true", matchIfMissing = true)
    public JvmThreadMetrics jvmThreadMetrics() {
        return new JvmThreadMetrics();
    }

    /**
     * Register ClassLoader metrics
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "io.joshuasalcedo.common.metrics", name = "classLoaderMetrics", havingValue = "true", matchIfMissing = true)
    public ClassLoaderMetrics classLoaderMetrics() {
        return new ClassLoaderMetrics();
    }

    /**
     * Register system processor metrics (CPU usage, etc.)
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "io.joshuasalcedo.common.metrics", name = "processorMetrics", havingValue = "true", matchIfMissing = true)
    public ProcessorMetrics processorMetrics() {
        return new ProcessorMetrics();
    }

    /**
     * Customize the meter registry to add common tags
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean
    public MeterRegistry meterRegistryCustomizer(MeterRegistry meterRegistry) {
        if (properties.getCommonTags() != null && !properties.getCommonTags().isEmpty()) {
            properties.getCommonTags().forEach((key, value) -> {
                meterRegistry.config().commonTags(key, value);
            });
        }
        return meterRegistry;
    }
}