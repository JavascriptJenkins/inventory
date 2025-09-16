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
            // todo: fix syntax errors in these files
//            executeSqlFile("01_batch_types.sql")
//            executeSqlFile("02_package_types.sql")
//            executeSqlFile("04_location_types.sql")
//            executeSqlFile("04_product_types.sql")

            // todo: this is how we can make tenant specific sql files to deploy
            // Load tenant-specific product types
//            String productTypesFile = "03_product_types_${tenant}.sql"
//            executeSqlFile(productTypesFile)
            
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
            
            // Remove comments and split by semicolon
            String[] lines = sqlContent.split("\n")
            StringBuilder currentStatement = new StringBuilder()
            
            for (String line : lines) {
                String trimmedLine = line.trim()
                
                // Skip empty lines and comment lines
                if (trimmedLine.isEmpty() || trimmedLine.startsWith("--")) {
                    continue
                }
                
                currentStatement.append(line).append("\n")
                
                // If line ends with semicolon, execute the statement
                if (trimmedLine.endsWith(";")) {
                    String statement = currentStatement.toString().trim()
                    if (!statement.isEmpty()) {
                        try {
                            jdbcTemplate.execute(statement)
                        } catch (Exception e) {
                            System.err.println("Error executing statement in ${fileName}: ${statement}")
                            System.err.println("Error: ${e.getMessage()}")
                            throw e
                        }
                    }
                    currentStatement = new StringBuilder()
                }
            }
            
            System.out.println("Executed SQL file: ${fileName}")
        } catch (Exception e) {
            System.err.println("Error executing SQL file ${fileName}: ${e.getMessage()}")
            throw e
        }
    }

}
