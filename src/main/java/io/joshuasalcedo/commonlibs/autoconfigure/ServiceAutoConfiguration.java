package io.joshuasalcedo.commonlibs.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.service", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ServiceAutoConfiguration {
    // Service configuration here
}