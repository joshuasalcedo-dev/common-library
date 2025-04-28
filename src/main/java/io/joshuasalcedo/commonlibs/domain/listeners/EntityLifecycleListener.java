package io.joshuasalcedo.commonlibs.domain.listeners;

import jakarta.persistence.*;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Entity lifecycle listener that notifies registered observers of entity changes.
 */
public class EntityLifecycleListener {

    private static final List<EntityLifecycleObserver> observers = new CopyOnWriteArrayList<>();
    
    public static void registerObserver(EntityLifecycleObserver observer) {
        observers.add(observer);
    }
    
    public static void removeObserver(EntityLifecycleObserver observer) {
        observers.remove(observer);
    }
    
    @PrePersist
    public void prePersist(Object entity) {
        notifyObservers(EntityLifecycleEvent.PRE_PERSIST, entity);
    }
    
    @PostPersist
    public void postPersist(Object entity) {
        notifyObservers(EntityLifecycleEvent.POST_PERSIST, entity);
    }
    
    @PreUpdate
    public void preUpdate(Object entity) {
        notifyObservers(EntityLifecycleEvent.PRE_UPDATE, entity);
    }
    
    @PostUpdate
    public void postUpdate(Object entity) {
        notifyObservers(EntityLifecycleEvent.POST_UPDATE, entity);
    }
    
    @PreRemove
    public void preRemove(Object entity) {
        notifyObservers(EntityLifecycleEvent.PRE_REMOVE, entity);
    }
    
    @PostRemove
    public void postRemove(Object entity) {
        notifyObservers(EntityLifecycleEvent.POST_REMOVE, entity);
    }
    
    @PostLoad
    public void postLoad(Object entity) {
        notifyObservers(EntityLifecycleEvent.POST_LOAD, entity);
    }
    
    private void notifyObservers(EntityLifecycleEvent event, Object entity) {
        for (EntityLifecycleObserver observer : observers) {
            if (observer.isInterestedIn(entity.getClass())) {
                observer.onEntityEvent(event, entity);
            }
        }
    }
}