package io.joshuasalcedo.commonlibs.autoconfigure;

import io.joshuasalcedo.commonlibs.domain.listeners.EntityLifecycleListener;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({EntityManagerFactory.class, EntityLifecycleListener.class})
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.lifecycle", name = "lifecycle-events-enabled", havingValue = "true", matchIfMissing = true)
public class EntityLifecycleAutoConfiguration {

    @Bean
    public EntityLifecycleListener entityLifecycleListener() {
        return new EntityLifecycleListener();
    }
}