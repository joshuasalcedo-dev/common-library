package io.joshuasalcedo.commonlibs.autoconfigure;

import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Configuration to set up datasource proxy for SQL query logging.
 * Using BeanPostProcessor to avoid circular dependencies.
 */
@Configuration
@ConditionalOnClass(name = "net.ttddyy.dsproxy.support.ProxyDataSourceBuilder")
@ConditionalOnProperty(prefix = "io.joshuasalcedo.common.logging", name = "sql-logging", havingValue = "true")
public class DataSourceProxyConfiguration {

    /**
     * Creates a BeanPostProcessor that processes the DataSource bean
     * after it has been fully initialized but before it's used by other beans.
     */
    @Bean
    public BeanPostProcessor dataSourceProxyBeanPostProcessor(
            final QueryExecutionListener queryExecutionListener,
            final LoggingProperties properties) {

        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof DataSource && properties.isSqlLogging()) {
                    return ProxyDataSourceBuilder
                            .create((DataSource) bean)
                            .name("SQL-Query-Logger")
                            .listener(queryExecutionListener)
                            .build();
                }
                return bean;
            }
        };
    }
}