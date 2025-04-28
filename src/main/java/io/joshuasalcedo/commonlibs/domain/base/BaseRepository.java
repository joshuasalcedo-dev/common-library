package io.joshuasalcedo.commonlibs.domain.base;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * Base repository interface that extends standard Spring Data JPA repository interfaces.
 * This provides common CRUD operations plus specification support for complex queries.
 *
 * @param <T> the entity type
 * @param <ID> the type of the entity's primary key
 */
@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity<ID>, ID extends Serializable>
        extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * Find an entity by its UUID.
     *
     * @param uuid the UUID to search for
     * @return the entity with the given UUID, or null if none found
     */
    T findByUuid(String uuid);
}