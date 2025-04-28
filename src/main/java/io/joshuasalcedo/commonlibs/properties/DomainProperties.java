package io.joshuasalcedo.commonlibs.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "io.joshuasalcedo.common.domain")
public class DomainProperties {
    /**
     * Base entity configuration
     */
    private EntityConfig entity = new EntityConfig();

    /**
     * Repository configuration
     */
    private RepositoryConfig repository = new RepositoryConfig();

    /**
     * Service configuration
     */
    private ServiceConfig service = new ServiceConfig();

    /**
     * Controller configuration
     */
    private ControllerConfig controller = new ControllerConfig();

    // Nested configuration classes
    public static class EntityConfig {


        /**
         * Enable or disable entity lifecycle events.
         */
        private boolean lifecycleEventsEnabled = true;

        /**
         * Enable or disable automatic entity auditing.
         */
        private boolean enableAuditing = true;

        /**
         * Default name to use for system actions when no user is authenticated.
         */
        private String systemUsername = "system";





        /**
         * Enable or disable soft delete functionality.
         */
        private boolean enableSoftDelete = true;

        // Getters and setters
        public boolean isEnableAuditing() {
            return enableAuditing;
        }

        public void setEnableAuditing(boolean enableAuditing) {
            this.enableAuditing = enableAuditing;
        }

        public String getSystemUsername() {
            return systemUsername;
        }

        public void setSystemUsername(String systemUsername) {
            this.systemUsername = systemUsername;
        }

        public boolean isEnableSoftDelete() {
            return enableSoftDelete;
        }

        public void setEnableSoftDelete(boolean enableSoftDelete) {
            this.enableSoftDelete = enableSoftDelete;
        }

        public boolean isLifecycleEventsEnabled() {
            return lifecycleEventsEnabled;
        }

        public void setLifecycleEventsEnabled(boolean lifecycleEventsEnabled) {
            this.lifecycleEventsEnabled = lifecycleEventsEnabled;
        }
    }

    public static class RepositoryConfig {
        /**
         * Enable or disable custom repository functionality.
         */
        private boolean enabled = true;

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class ServiceConfig {
        /**
         * Enable or disable service layer.
         */
        private boolean enabled = true;

        /**
         * Enable validation at service layer.
         */
        private boolean enableValidation = true;

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnableValidation() {
            return enableValidation;
        }

        public void setEnableValidation(boolean enableValidation) {
            this.enableValidation = enableValidation;
        }
    }

    public static class ControllerConfig {
        /**
         * Enable or disable generic controllers.
         */
        private boolean enabled = true;

        /**
         * Base path for REST endpoints.
         */
        private String basePath = "/api";

        // Getters and setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
    }

    // Getters and setters for the main config
    public EntityConfig getEntity() {
        return entity;
    }

    public void setEntity(EntityConfig entity) {
        this.entity = entity;
    }

    public RepositoryConfig getRepository() {
        return repository;
    }

    public void setRepository(RepositoryConfig repository) {
        this.repository = repository;
    }

    public ServiceConfig getService() {
        return service;
    }

    public void setService(ServiceConfig service) {
        this.service = service;
    }

    public ControllerConfig getController() {
        return controller;
    }

    public void setController(ControllerConfig controller) {
        this.controller = controller;
    }
}