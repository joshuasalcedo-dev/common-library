package io.joshuasalcedo.commonlibs.autoconfigure;

import io.joshuasalcedo.commonlibs.domain.GlobalExceptionHandler;
import io.joshuasalcedo.commonlibs.domain.logging.LoggingFactory;
import io.joshuasalcedo.commonlibs.domain.logging.LoggingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for controllers and exception handling.
 */
@Configuration
@ConditionalOnWebApplication
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.controller", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ControllerAutoConfiguration {



    /**
     * Creates a global exception handler bean for REST controllers.
     * This provides consistent error responses across the application.
     */
    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler() {

        return new GlobalExceptionHandler();
    }
}