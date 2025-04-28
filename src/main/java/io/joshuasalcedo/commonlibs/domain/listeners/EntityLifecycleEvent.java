package io.joshuasalcedo.commonlibs.domain.listeners;

/**
 * Types of entity lifecycle events.
 */
public enum EntityLifecycleEvent {
    PRE_PERSIST,
    POST_PERSIST,
    PRE_UPDATE,
    POST_UPDATE,
    PRE_REMOVE,
    POST_REMOVE,
    POST_LOAD
}