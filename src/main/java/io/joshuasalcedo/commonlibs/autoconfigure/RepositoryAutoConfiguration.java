package io.joshuasalcedo.commonlibs.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;


/**
 * Auto-configuration for repository components.
 * This configuration enables JPA repositories and entity scanning.
 */
@Configuration
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.repository", name = "enabled", havingValue = "true", matchIfMissing = true)
public class RepositoryAutoConfiguration {

    /**
     * Repository configuration that allows applications to define their own repository packages.
     * This configuration doesn't force any specific package structure on the application.
     */
    @Configuration
    public static class DefaultRepositoryConfiguration {
        // This is intentionally left empty to allow Spring Boot's auto-configuration
        // to set up repositories based on the application's own configuration
    }
}