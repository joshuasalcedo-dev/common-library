package io.joshuasalcedo.commonlibs.autoconfigure;

import io.joshuasalcedo.commonlibs.EntityUtil;
import io.joshuasalcedo.commonlibs.listeners.AuditingEntityListener;
import io.joshuasalcedo.commonlibs.properties.DomainProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Auto-configuration for entity functionality.
 */
@Configuration
@EnableConfigurationProperties(DomainProperties.class)
public class EntityAutoConfiguration {

    private final DomainProperties properties;

    public EntityAutoConfiguration(DomainProperties properties) {
        this.properties = properties;
    }

    /**
     * Create and configure an AuditingEntityListener bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public AuditingEntityListener auditingEntityListener() {
        AuditingEntityListener listener = new AuditingEntityListener();
        listener.setEntityProperties(properties.getEntity());
        return listener;
    }

    /**
     * Configuration for JPA auditing.
     */
    @Configuration
    @ConditionalOnProperty(prefix = "io.joshuasalcedo.common.domain.entity", name = "enable-auditing", havingValue = "true", matchIfMissing = true)
    @EnableJpaAuditing(auditorAwareRef = "auditorProvider")
    public static class AuditingConfiguration {

        private final DomainProperties properties;

        public AuditingConfiguration(DomainProperties properties) {
            this.properties = properties;
        }

        /**
         * Provides the current auditor for JPA auditing.
         */
        @Bean
        @ConditionalOnMissingBean
        public AuditorAware<String> auditorProvider() {
            return () -> Optional.of(properties.getEntity().getSystemUsername());
        }
    }

    /**
     * Create an EntityUtil bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public EntityUtil entityUtil() {
        return new EntityUtil();
    }
}