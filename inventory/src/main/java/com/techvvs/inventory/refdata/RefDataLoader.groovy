package com.techvvs.inventory.refdata

import com.techvvs.inventory.constants.AppConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

import java.nio.charset.StandardCharsets
import java.nio.file.Files

// The purpose of this class is to load reference data on startup using SQL files
@Component
class RefDataLoader {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    Environment environment

    @Autowired
    AppConstants appConstants


    void loadRefData(){
        String tenant = environment.getProperty("techvvs.tenant") ?: "local"
        System.out.println("Loading reference data for tenant: " + tenant)
        
        try {
            // Load common reference data files
            executeSqlFile("01_batch_types.sql")
            executeSqlFile("02_package_types.sql")
            executeSqlFile("04_location_types.sql")
            executeSqlFile("05_lockers.sql")
            
            // Load tenant-specific product types
            String productTypesFile = "03_product_types_${tenant}.sql"
            executeSqlFile(productTypesFile)
            
            System.out.println("Reference data loaded successfully for tenant: " + tenant)
        } catch (Exception e) {
            System.err.println("Error loading reference data: " + e.getMessage())
            e.printStackTrace()
        }
    }

    /**
     * Execute SQL file from the refdata directory
     * @param fileName The SQL file name to execute
     */
    private void executeSqlFile(String fileName) {
        try {
            ClassPathResource resource = new ClassPathResource("static/refdata/${fileName}")
            if (!resource.exists()) {
                System.out.println("SQL file not found: ${fileName}")
                return
            }
            
            String sqlContent = new String(Files.readAllBytes(resource.getFile().toPath()), StandardCharsets.UTF_8)
            
            // Split SQL content by semicolon and execute each statement
            String[] statements = sqlContent.split(";")
            for (String statement : statements) {
                String trimmedStatement = statement.trim()
                if (!trimmedStatement.isEmpty()) {
                    jdbcTemplate.execute(trimmedStatement)
                }
            }
            
            System.out.println("Executed SQL file: ${fileName}")
        } catch (Exception e) {
            System.err.println("Error executing SQL file ${fileName}: ${e.getMessage()}")
            throw e
        }
    }

}
