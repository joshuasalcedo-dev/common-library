package io.joshuasalcedo.commonlibs.domain.base.controller;

import io.joshuasalcedo.commonlibs.domain.base.BaseEntity;
import io.joshuasalcedo.commonlibs.domain.ResourceNotFoundException;
import io.joshuasalcedo.commonlibs.domain.base.dto.ResponseDTO;
import io.joshuasalcedo.commonlibs.domain.base.service.BaseService;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.Serializable;
import java.util.List;

/**
 * Base implementation of BaseController that provides standard REST endpoints.
 *
 * @param <T> the entity type
 * @param <S> the service type
 * @param <ID> the type of the entity's primary key
 */
public class CrudBaseController<T extends BaseEntity<ID>, S extends BaseService<T, ID>, ID extends Serializable>
        implements BaseController<T, ID> {

    protected final S service;
    protected final Class<T> entityClass;

    public CrudBaseController(S service, Class<T> entityClass) {
        this.service = service;
        this.entityClass = entityClass;
    }

    @Override
    public ResponseEntity<ResponseDTO<T>> create(@Valid @RequestBody T entity) {
        T savedEntity = service.save(entity);
        return new ResponseEntity<>(
                ResponseDTO.success(savedEntity, entityClass.getSimpleName() + " created successfully"),
                HttpStatus.CREATED
        );
    }

    @Override
    public ResponseEntity<ResponseDTO<T>> findById(@PathVariable ID id) {
        return service.findById(id)
                .map(entity -> ResponseEntity.ok(ResponseDTO.success(entity)))
                .orElseThrow(() -> new ResourceNotFoundException(entityClass.getSimpleName() + " not found with id: " + id));
    }

    @Override
    public ResponseEntity<ResponseDTO<T>> findByUuid(@PathVariable String uuid) {
        return service.findByUuid(uuid)
                .map(entity -> ResponseEntity.ok(ResponseDTO.success(entity)))
                .orElseThrow(() -> new ResourceNotFoundException(entityClass.getSimpleName() + " not found with uuid: " + uuid));
    }

    @Override
    public ResponseEntity<ResponseDTO<List<T>>> findAll() {
        List<T> entities = service.findAll();
        return ResponseEntity.ok(ResponseDTO.success(entities));
    }

    @Override
    public ResponseEntity<ResponseDTO<Page<T>>> findAll(Pageable pageable) {
        Page<T> page = service.findAll(pageable);
        return ResponseEntity.ok(ResponseDTO.success(page));
    }

    @Override
    public ResponseEntity<ResponseDTO<T>> update(@PathVariable ID id, @Valid @RequestBody T entity) {
        // Ensure the entity exists
        if (!service.existsById(id)) {
            throw new ResourceNotFoundException(entityClass.getSimpleName() + " not found with id: " + id);
        }

        // Set the ID if it's not already set
        if (entity.getId() == null) {
            entity.setId(id);
        }

        T updatedEntity = service.save(entity);
        return ResponseEntity.ok(
                ResponseDTO.success(updatedEntity, entityClass.getSimpleName() + " updated successfully")
        );
    }

    @Override
    public ResponseEntity<ResponseDTO<Void>> deleteById(@PathVariable ID id) {
        // Ensure the entity exists
        if (!service.existsById(id)) {
            throw new ResourceNotFoundException(entityClass.getSimpleName() + " not found with id: " + id);
        }

        service.deleteById(id);
        return ResponseEntity.ok(
                ResponseDTO.success(null, entityClass.getSimpleName() + " deleted successfully")
        );
    }
}