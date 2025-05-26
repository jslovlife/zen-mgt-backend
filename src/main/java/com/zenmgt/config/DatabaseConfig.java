package com.zenmgt.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Basic connection settings
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        
        // Connection pool settings for better thread handling
        config.setMinimumIdle(5);                    // Minimum number of idle connections
        config.setMaximumPoolSize(20);               // Maximum number of connections in pool
        config.setConnectionTimeout(30000);          // 30 seconds timeout for getting connection
        config.setIdleTimeout(600000);               // 10 minutes idle timeout
        config.setMaxLifetime(1800000);              // 30 minutes max lifetime
        config.setLeakDetectionThreshold(60000);     // 1 minute leak detection
        
        // Performance optimizations using data source properties
        config.setPoolName("ZenMgt-HikariCP");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        // Thread safety and validation
        config.setValidationTimeout(5000);           // 5 seconds validation timeout
        config.setConnectionTestQuery("SELECT 1");
        config.setInitializationFailTimeout(1);
        
        // Custom thread factory for HikariCP
        config.setThreadFactory(r -> {
            Thread thread = new Thread(r);
            thread.setName("ZenMgt-HikariCP-" + thread.getId());
            thread.setDaemon(true);
            return thread;
        });
        
        return new HikariDataSource(config);
    }
} 