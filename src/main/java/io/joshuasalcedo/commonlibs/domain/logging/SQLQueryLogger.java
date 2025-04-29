package io.joshuasalcedo.commonlibs.domain.logging;


import io.joshuasalcedo.commonlibs.properties.LoggingProperties;
import net.ttddyy.dsproxy.ExecutionInfo;
import net.ttddyy.dsproxy.QueryInfo;
import net.ttddyy.dsproxy.listener.QueryExecutionListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SQL query logger that logs database queries when enabled.
 * Uses datasource-proxy to intercept and log SQL queries.
 */
@Component
@ConditionalOnClass(name = "net.ttddyy.dsproxy.listener.QueryExecutionListener")
@ConditionalOnProperty(prefix = "io.joshuasalcedo.logging", name = "sql-logging", havingValue = "true")
public class SQLQueryLogger implements QueryExecutionListener {

    private final LoggingService logger;
    private final boolean enabled;

    public SQLQueryLogger(LoggingService logger, LoggingProperties properties) {
        this.logger = logger;
        this.enabled = properties.isSqlLogging();
    }

    @Override
    public void beforeQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        // No action needed before query execution
    }

    @Override
    public void afterQuery(ExecutionInfo execInfo, List<QueryInfo> queryInfoList) {
        if (!enabled || !logger.isDebugEnabled()) {
            return;
        }

        // Log each query with its execution time
        for (QueryInfo queryInfo : queryInfoList) {
            String query = queryInfo.getQuery();
            
            // Format and log the query
            logger.debug("SQL Query executed in {} ms:\n{}",
                         execInfo.getElapsedTime(),
                         formatQuery(query));
            
            // Log parameters if present
            logger.debug("Query parameters: {}", queryInfo.getParametersList().stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", ")));
        }
    }
    
    /**
     * Format the SQL query for better readability in logs.
     */
    private String formatQuery(String query) {
        // Simple SQL formatting - in a real implementation, 
        // you might use a SQL formatter library
        return query.replaceAll("(?i)\\s+(FROM|WHERE|JOIN|AND|OR|GROUP BY|ORDER BY|HAVING)\\s+", 
                               "\n$1 ");
    }
}