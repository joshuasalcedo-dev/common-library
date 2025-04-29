package io.joshuasalcedo.commonlibs.autoconfigure;

import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Configuration to set up datasource proxy for SQL query logging.
 */
@Configuration
@ConditionalOnClass(name = "net.ttddyy.dsproxy.support.ProxyDataSourceBuilder")
@ConditionalOnProperty(prefix = "io.joshuasalcedo.logging", name = "sql-logging", havingValue = "true")
public class DataSourceProxyConfiguration {

    /**
     * Create a proxy around the existing DataSource to intercept
     * SQL queries for logging.
     */
    @Bean
    @Primary
    public DataSource proxyDataSource(DataSource dataSource, 
                                     QueryExecutionListener queryExecutionListener,
                                     LoggingProperties properties) {
        
        if (!properties.isSqlLogging()) {
            return dataSource;
        }
        
        return ProxyDataSourceBuilder
                .create(dataSource)
                .name("SQL-Query-Logger")
                .listener(queryExecutionListener)
                .build();
    }
}