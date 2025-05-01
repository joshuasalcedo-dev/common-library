package io.joshuasalcedo.commonlibs.autoconfigure;

import io.joshuasalcedo.commonlibs.config.SecurityConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Auto-configuration for Security.
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnClass({HttpSecurity.class, SecurityFilterChain.class})
@ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import(SecurityConfig.class)
public class SecurityAutoConfiguration {
    // No additional beans needed - imports SecurityConfig
}