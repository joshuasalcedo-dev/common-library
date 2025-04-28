package io.joshuasalcedo.commonlibs.domain.base.service;

import io.joshuasalcedo.commonlibs.domain.base.BaseEntity;
import io.joshuasalcedo.commonlibs.domain.base.BaseRepository;
import io.joshuasalcedo.commonlibs.domain.base.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Base implementation of BaseService that provides standard CRUD operations.
 *
 * @param <T> the entity type
 * @param <R> the repository type
 * @param <ID> the type of the entity's primary key
 */
@Validated
@Transactional
public class CrudBaseService<T extends BaseEntity<ID>, R extends BaseRepository<T, ID>, ID extends Serializable>
        implements BaseService<T, ID> {

    protected final R repository;

    public CrudBaseService(R repository) {
        this.repository = repository;
    }

    @Override
    public T save(@Valid T entity) {
        return repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<T> findByUuid(String uuid) {
        return Optional.ofNullable(repository.findByUuid(uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public List<T> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<T> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    public void deleteById(ID id) {
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }

    /**
     * Utility method to get an entity by ID or throw an exception if not found.
     *
     * @param id the ID of the entity to find
     * @return the entity if found
     * @throws ResourceNotFoundException if the entity is not found
     */
    protected T getEntityById(ID id) {
        return findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entity not found with id: " + id));
    }
}