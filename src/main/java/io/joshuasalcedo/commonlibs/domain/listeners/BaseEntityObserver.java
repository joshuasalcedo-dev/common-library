package io.joshuasalcedo.commonlibs.domain.listeners;

import io.joshuasalcedo.commonlibs.domain.base.BaseEntity;
import io.joshuasalcedo.commonlibs.domain.logging.LoggingManager;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Base implementation of EntityLifecycleObserver that provides common functionality
 * for entity lifecycle event observation.
 * 
 * This class simplifies creating custom entity observers by providing:
 * - Type-safe event handling methods for each lifecycle event
 * - Automatic type checking and casting
 * - Built-in error handling
 * - Configurable entity class filtering
 */
public abstract class BaseEntityObserver implements EntityLifecycleObserver {

    private final Set<Class<?>> observedEntityClasses;

    /**
     * Create a base observer for the specified entity classes.
     * 
     * @param entityClasses the entity classes to observe
     */
    protected BaseEntityObserver(Class<?>... entityClasses) {
        this.observedEntityClasses = new HashSet<>(Arrays.asList(entityClasses));
    }

    /**
     * Check if this observer is interested in events for the given entity class.
     * 
     * @param entityClass The class of the entity
     * @return true if this observer is configured to observe the entity class, false otherwise
     */
    @Override
    public boolean isInterestedIn(Class<?> entityClass) {
        return observedEntityClasses.isEmpty() || observedEntityClasses.contains(entityClass);
    }

    /**
     * Handle an entity lifecycle event. This method delegates to the appropriate
     * event-specific method based on the event type.
     * 
     * @param event The lifecycle event
     * @param entity The entity that triggered the event
     */
    @Override
    public void onEntityEvent(EntityLifecycleEvent event, Object entity) {
        try {
            // Ensure the entity is a BaseEntity
            if (!(entity instanceof BaseEntity)) {
                // Skip non-BaseEntity objects
                return;
            }

            BaseEntity<?> baseEntity = (BaseEntity<?>) entity;
            
            // Delegate to the appropriate event handler
            switch (event) {
                case PRE_PERSIST:
                    onPrePersist(baseEntity);
                    break;
                case POST_PERSIST:
                    onPostPersist(baseEntity);
                    break;
                case PRE_UPDATE:
                    onPreUpdate(baseEntity);
                    break;
                case POST_UPDATE:
                    onPostUpdate(baseEntity);
                    break;
                case PRE_REMOVE:
                    onPreRemove(baseEntity);
                    break;
                case POST_REMOVE:
                    onPostRemove(baseEntity);
                    break;
                case POST_LOAD:
                    onPostLoad(baseEntity);
                    break;
                default:
                    // Do nothing for unknown events
            }
        }    catch (Exception e) {
            // Get a logger from LoggingManager to avoid circular dependencies
            LoggingManager.getLogger(BaseEntityObserver.class).error("Error in entity lifecycle event handling", e);
        }

    }

    /**
     * Called before an entity is persisted.
     * Override this method to handle pre-persist events.
     * 
     * @param entity the entity that will be persisted
     */
    protected void onPrePersist(BaseEntity<?> entity) {
        // Default implementation does nothing
    }

    /**
     * Called after an entity is persisted.
     * Override this method to handle post-persist events.
     * 
     * @param entity the persisted entity
     */
    protected void onPostPersist(BaseEntity<?> entity) {
        // Default implementation does nothing
    }

    /**
     * Called before an entity is updated.
     * Override this method to handle pre-update events.
     * 
     * @param entity the entity that will be updated
     */
    protected void onPreUpdate(BaseEntity<?> entity) {
        // Default implementation does nothing
    }

    /**
     * Called after an entity is updated.
     * Override this method to handle post-update events.
     * 
     * @param entity the updated entity
     */
    protected void onPostUpdate(BaseEntity<?> entity) {
        // Default implementation does nothing
    }

    /**
     * Called before an entity is removed.
     * Override this method to handle pre-remove events.
     * 
     * @param entity the entity that will be removed
     */
    protected void onPreRemove(BaseEntity<?> entity) {
        // Default implementation does nothing
    }

    /**
     * Called after an entity is removed.
     * Override this method to handle post-remove events.
     * 
     * @param entity the removed entity
     */
    protected void onPostRemove(BaseEntity<?> entity) {
        // Default implementation does nothing
    }

    /**
     * Called after an entity is loaded from the database.
     * Override this method to handle post-load events.
     * 
     * @param entity the loaded entity
     */
    protected void onPostLoad(BaseEntity<?> entity) {
        // Default implementation does nothing
    }
}