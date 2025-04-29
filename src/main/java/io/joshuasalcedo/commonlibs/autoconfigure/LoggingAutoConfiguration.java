package io.joshuasalcedo.commonlibs.autoconfigure;


import io.joshuasalcedo.commonlibs.domain.logging.LoggingAspect;
import io.joshuasalcedo.commonlibs.domain.logging.LoggingFactory;
import io.joshuasalcedo.commonlibs.domain.logging.LoggingManager;
import io.joshuasalcedo.commonlibs.domain.logging.LoggingService;
import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * Auto-configuration for the logging system.
 */
@Configuration
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.logging", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableAspectJAutoProxy

public class LoggingAutoConfiguration {

    private final LoggingProperties properties;

    public LoggingAutoConfiguration(LoggingProperties properties) {
        this.properties = properties;
    }

    /**
     * Create the LoggingFactory bean and initialize the LoggingManager.
     */
    @Bean
    @ConditionalOnMissingBean
    public LoggingFactory loggingFactory() {
        LoggingFactory factory = new LoggingFactory(properties);
        LoggingManager.setLoggingFactory(factory);
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public LoggingService defaultLoggingService(LoggingFactory loggingFactory) {
        return loggingFactory.getLogger(LoggingAutoConfiguration.class);
    }

    /**
     * Create the LoggingAspect bean for AOP logging.
     */
    @Bean
    @ConditionalOnMissingBean
    public LoggingAspect loggingAspect(LoggingFactory loggingFactory) {
        return new LoggingAspect(loggingFactory, properties);
    }

    /**
     * Create a CommonsRequestLoggingFilter bean for HTTP request logging.
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnProperty(prefix = "io.joshuasalcedo.common.logging", name = "request-logging", havingValue = "true", matchIfMissing = true)
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(true);
        filter.setMaxPayloadLength(10000);
        filter.setIncludeHeaders(false);
        filter.setAfterMessagePrefix("REQUEST DATA: ");
        return filter;
    }

}