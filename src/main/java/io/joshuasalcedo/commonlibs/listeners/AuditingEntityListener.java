package io.joshuasalcedo.commonlibs.listeners;


import io.joshuasalcedo.commonlibs.domain.base.BaseEntity;
import io.joshuasalcedo.commonlibs.properties.DomainProperties;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Data;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Entity listener that handles auditing fields automatically.
 */
@Data
@Component
public class AuditingEntityListener {

    private DomainProperties.EntityConfig entityProperties;

    // Default constructor required for JPA
    public AuditingEntityListener() {
    }

    @PrePersist
    public void setCreationAuditFields(Object entity) {
        if (entity instanceof BaseEntity<?> baseEntity) {

            if (baseEntity.getCreatedAt() == null) {
                baseEntity.setCreatedAt(LocalDateTime.now());
            }

            if (baseEntity.getCreatedBy() == null) {
                baseEntity.setCreatedBy(getCurrentAuditor());
            }

            baseEntity.setUpdatedAt(baseEntity.getCreatedAt());
            baseEntity.setUpdatedBy(baseEntity.getCreatedBy());
        }
    }

    @PreUpdate
    public void setUpdateAuditFields(Object entity) {
        if (entity instanceof BaseEntity<?> baseEntity) {

            baseEntity.setUpdatedAt(LocalDateTime.now());
            baseEntity.setUpdatedBy(getCurrentAuditor());
        }
    }

    private String getCurrentAuditor() {
        if (entityProperties == null) {
            return "system";
        }
        return entityProperties.getSystemUsername();
    }
}