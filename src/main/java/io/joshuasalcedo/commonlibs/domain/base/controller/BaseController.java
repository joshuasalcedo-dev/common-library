package io.joshuasalcedo.commonlibs.domain.base.controller;

import io.joshuasalcedo.commonlibs.domain.base.BaseEntity;
import io.joshuasalcedo.commonlibs.domain.base.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;

/**
 * Base controller interface that defines standard REST endpoints for CRUD operations.
 *
 * @param <T> the entity type
 * @param <ID> the type of the entity's primary key
 */
@Tag(name = "Base Controller", description = "Standard CRUD operations")
public interface BaseController<T extends BaseEntity<ID>, ID extends Serializable> {

    @Operation(summary = "Create a new entity", description = "Creates a new entity with the provided details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Entity created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    ResponseEntity<ResponseDTO<T>> create(@RequestBody T entity);

    @Operation(summary = "Get entity by ID", description = "Retrieves an entity by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entity found"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<ResponseDTO<T>> findById(
            @Parameter(description = "ID of the entity to retrieve") @PathVariable ID id);

    @Operation(summary = "Get entity by UUID", description = "Retrieves an entity by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entity found"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @GetMapping("/uuid/{uuid}")
    ResponseEntity<ResponseDTO<T>> findByUuid(
            @Parameter(description = "UUID of the entity to retrieve") @PathVariable String uuid);

    @Operation(summary = "List all entities", description = "Retrieves all entities")
    @ApiResponse(responseCode = "200", description = "List of entities")
    @GetMapping
    ResponseEntity<ResponseDTO<List<T>>> findAll();

    @Operation(summary = "List all entities (paginated)", description = "Retrieves entities with pagination")
    @ApiResponse(responseCode = "200", description = "Page of entities")
    @GetMapping("/page")
    ResponseEntity<ResponseDTO<Page<T>>> findAll(Pageable pageable);

    @Operation(summary = "Update entity by ID", description = "Updates an existing entity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entity updated successfully"),
            @ApiResponse(responseCode = "404", description = "Entity not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    ResponseEntity<ResponseDTO<T>> update(
            @Parameter(description = "ID of the entity to update") @PathVariable ID id,
            @RequestBody T entity);

    @Operation(summary = "Delete entity by ID", description = "Permanently deletes an entity")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Entity deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Entity not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<ResponseDTO<Void>> deleteById(
            @Parameter(description = "ID of the entity to delete") @PathVariable ID id);
}