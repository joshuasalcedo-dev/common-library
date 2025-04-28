package io.joshuasalcedo.commonlibs.domain.listeners;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EntityLifecycleObserverRegistrar implements BeanPostProcessor , DisposableBean {
    // Keep track of registered observers
    private final List<EntityLifecycleObserver> registeredObservers = new ArrayList<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        if (bean instanceof EntityLifecycleObserver) {
            EntityLifecycleListener.registerObserver((EntityLifecycleObserver) bean);
        }
        return bean;
    }

    @Override
    public void destroy() {
        // Clean up when the Spring context is closed
        registeredObservers.forEach(EntityLifecycleListener::removeObserver);
        registeredObservers.clear();
    }
}