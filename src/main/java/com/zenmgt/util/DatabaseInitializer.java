package com.zenmgt.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Database initializer to run schema and data migrations on startup
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Check if auth_user table exists
            String checkTableQuery = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'zen_mgt' AND table_name = 'auth_user'";
            Integer tableCount = jdbcTemplate.queryForObject(checkTableQuery, Integer.class);
            
            if (tableCount != null && tableCount == 0) {
                log.info("Tables not found. Running database migration scripts...");
                
                // Run schema migration
                ClassPathResource schemaResource = new ClassPathResource("db/migration/1_schema.sql");
                String schemaSQL = FileCopyUtils.copyToString(new InputStreamReader(schemaResource.getInputStream(), StandardCharsets.UTF_8));
                
                // Split by semicolon and execute each statement
                String[] statements = schemaSQL.split(";");
                for (String statement : statements) {
                    statement = statement.trim();
                    if (!statement.isEmpty()) {
                        jdbcTemplate.execute(statement);
                    }
                }
                log.info("Schema migration completed successfully");
                
                // Run data migration
                ClassPathResource dataResource = new ClassPathResource("db/migration/2_coredata.sql");
                String dataSQL = FileCopyUtils.copyToString(new InputStreamReader(dataResource.getInputStream(), StandardCharsets.UTF_8));
                
                // Split by semicolon and execute each statement
                String[] dataStatements = dataSQL.split(";");
                for (String statement : dataStatements) {
                    statement = statement.trim();
                    if (!statement.isEmpty() && !statement.startsWith("--")) {
                        try {
                            jdbcTemplate.execute(statement);
                        } catch (Exception e) {
                            log.warn("Failed to execute statement (continuing): {}", e.getMessage());
                        }
                    }
                }
                log.info("Data migration completed successfully");
                
            } else {
                log.info("Database tables already exist. Skipping migration.");
            }
            
        } catch (Exception e) {
            log.error("Error during database initialization: {}", e.getMessage(), e);
        }
    }
} 