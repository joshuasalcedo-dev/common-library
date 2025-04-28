package io.joshuasalcedo.commonlibs.domain.base.service;

import io.joshuasalcedo.commonlibs.domain.base.BaseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Base service interface for CRUD operations on entities.
 *
 * @param <T> the entity type
 * @param <ID> the type of the entity's primary key
 */
public interface BaseService<T extends BaseEntity<ID>, ID extends Serializable> {

    /**
     * Save a new entity or update an existing one.
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    T save(T entity);

    /**
     * Find an entity by its ID.
     *
     * @param id the ID to search for
     * @return an Optional containing the entity if found, or empty if not found
     */
    Optional<T> findById(ID id);

    /**
     * Find an entity by its UUID.
     *
     * @param uuid the UUID to search for
     * @return an Optional containing the entity if found, or empty if not found
     */
    Optional<T> findByUuid(String uuid);

    /**
     * Find all entities.
     *
     * @return a list of all entities
     */
    List<T> findAll();

    /**
     * Find all entities with pagination.
     *
     * @param pageable pagination information
     * @return a page of entities
     */
    Page<T> findAll(Pageable pageable);

    /**
     * Delete an entity by its ID.
     *
     * @param id the ID of the entity to delete
     */
    void deleteById(ID id);

    /**
     * Check if an entity exists by its ID.
     *
     * @param id the ID to check
     * @return true if an entity with the given ID exists, false otherwise
     */
    boolean existsById(ID id);
}