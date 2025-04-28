package io.joshuasalcedo.commonlibs.domain.listeners;

/**
 * Interface for observers of entity lifecycle events.
 */
public interface EntityLifecycleObserver {
    
    /**
     * Check if this observer is interested in events for the given entity class.
     *
     * @param entityClass The class of the entity
     * @return true if interested, false otherwise
     */
    boolean isInterestedIn(Class<?> entityClass);
    
    /**
     * Handle an entity lifecycle event.
     *
     * @param event The lifecycle event
     * @param entity The entity that triggered the event
     */
    void onEntityEvent(EntityLifecycleEvent event, Object entity);
}