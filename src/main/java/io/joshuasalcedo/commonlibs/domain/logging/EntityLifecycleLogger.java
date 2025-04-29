package io.joshuasalcedo.commonlibs.domain.logging;

import io.joshuasalcedo.commonlibs.domain.base.BaseEntity;
import io.joshuasalcedo.commonlibs.domain.listeners.BaseEntityObserver;
import io.joshuasalcedo.commonlibs.domain.listeners.EntityLifecycleEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Entity lifecycle observer that logs entity lifecycle events.
 */
@Component
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.logging", name = "entity-lifecycle-logging", havingValue = "true", matchIfMissing = true)
public class EntityLifecycleLogger extends BaseEntityObserver {
    
    private final LoggingService logger;
    
    public EntityLifecycleLogger(LoggingFactory loggingFactory) {
        // This observer is interested in all entity types
        super();
        this.logger = loggingFactory.getLogger(EntityLifecycleLogger.class);
    }
    
    @Override
    protected void onPrePersist(BaseEntity<?> entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entity PRE_PERSIST: {} (UUID: {})", 
                entity.getClass().getSimpleName(), entity.getUuid());
        }
    }
    
    @Override
    protected void onPostPersist(BaseEntity<?> entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entity POST_PERSIST: {} (UUID: {}, ID: {})", 
                entity.getClass().getSimpleName(), entity.getUuid(), entity.getId());
        }
    }
    
    @Override
    protected void onPreUpdate(BaseEntity<?> entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entity PRE_UPDATE: {} (UUID: {}, ID: {})", 
                entity.getClass().getSimpleName(), entity.getUuid(), entity.getId());
        }
    }
    
    @Override
    protected void onPostUpdate(BaseEntity<?> entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entity POST_UPDATE: {} (UUID: {}, ID: {})", 
                entity.getClass().getSimpleName(), entity.getUuid(), entity.getId());
        }
    }
    
    @Override
    protected void onPreRemove(BaseEntity<?> entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entity PRE_REMOVE: {} (UUID: {}, ID: {})", 
                entity.getClass().getSimpleName(), entity.getUuid(), entity.getId());
        }
    }
    
    @Override
    protected void onPostRemove(BaseEntity<?> entity) {
        if (logger.isDebugEnabled()) {
            logger.debug("Entity POST_REMOVE: {} (UUID: {}, ID: {})", 
                entity.getClass().getSimpleName(), entity.getUuid(), entity.getId());
        }
    }
    
    @Override
    protected void onPostLoad(BaseEntity<?> entity) {
        if (logger.isTraceEnabled()) {
            logger.trace("Entity POST_LOAD: {} (UUID: {}, ID: {})", 
                entity.getClass().getSimpleName(), entity.getUuid(), entity.getId());
        }
    }
}