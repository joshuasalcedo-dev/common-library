package io.joshuasalcedo.commonlibs.domain.base;

import io.joshuasalcedo.commonlibs.domain.listeners.EntityLifecycleListener;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Base abstract entity class that provides common fields and functionality
 * for all domain entities in the application.
 *
 * @param <ID> The type of the entity's primary key
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class, EntityLifecycleListener.class})
public abstract class BaseEntity<ID extends Serializable> implements Serializable {



    @Serial
    @Builder.Default
    private static final long serialVersionUID = 1L;

    /**
     * Unique business identifier for the entity that remains consistent
     * across systems and is safe to expose externally.
     */
    @Column(name = "uuid", unique = true, nullable = false, updatable = false)
    @Builder.Default
    private String uuid = UUID.randomUUID().toString();

    /**
     * Version field for optimistic locking to prevent concurrent modifications.
     */
    @Version
    @Column(name = "version")
    @Builder.Default  // Add this annotation
    private Long version = 0L;

    /**
     * Flag indicating if the entity is active.
     * Used for soft deletion.
     */
    @Column(name = "active")
    @Builder.Default  // Add this annotation
    private boolean active = true;

    /**
     * Timestamp when the entity was created.
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Identifier of the user who created the entity.
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    /**
     * Timestamp when the entity was last updated.
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Identifier of the user who last updated the entity.
     */
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    /**
     * Returns the ID of the entity. Must be implemented by subclasses.
     *
     * @return the ID of the entity
     */
    public abstract ID getId();

    /**
     * Sets the ID of the entity. Must be implemented by subclasses.
     *
     * @param id the ID to set
     */
    public abstract void setId(ID id);

    /**
     * Marker method to check if the entity is new (not yet persisted).
     *
     * @return true if the entity is new, false otherwise
     */
    @Transient
    public boolean isNew() {
        return getId() == null;
    }

    /**
     * Pre-persist lifecycle callback to set initial values before persisting.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = createdAt;
    }

    /**
     * Pre-update lifecycle callback to update timestamps before updating.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Method to soft delete an entity by setting active to false.
     */
    public void softDelete() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity<?> that = (BaseEntity<?>) o;
        return Objects.equals(uuid, that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}