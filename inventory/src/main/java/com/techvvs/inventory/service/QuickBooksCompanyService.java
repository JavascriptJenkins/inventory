package com.techvvs.inventory.service;

import com.techvvs.inventory.model.QuickBooksCompany;
import com.techvvs.inventory.repository.QuickBooksCompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class QuickBooksCompanyService {
    
    private static final Logger logger = LoggerFactory.getLogger(QuickBooksCompanyService.class);
    
    @Autowired
    private QuickBooksCompanyRepository quickBooksCompanyRepository;
    
    private static final String SANDBOX_ENV = "SANDBOX";
    private static final String PROD_ENV = "PROD";
    
    /**
     * Get all companies for an environment
     */
    public List<QuickBooksCompany> getCompaniesByEnvironment(String environment) {
        return quickBooksCompanyRepository.findByEnvironmentOrderByCreatedAtDesc(environment);
    }
    
    /**
     * Get active company for an environment
     */
    public Optional<QuickBooksCompany> getActiveCompany(String environment) {
        return quickBooksCompanyRepository.findByEnvironmentAndIsActiveTrue(environment);
    }
    
    /**
     * Add a new company
     */
    @Transactional
    public QuickBooksCompany addCompany(String companyId, String companyName, String environment, Boolean isSandboxCreated) {
        // Check if company already exists
        if (quickBooksCompanyRepository.existsByCompanyIdAndEnvironment(companyId, environment)) {
            throw new IllegalArgumentException("Company with ID " + companyId + " already exists for environment " + environment);
        }
        
        QuickBooksCompany company = new QuickBooksCompany(companyId, companyName, environment, isSandboxCreated);
        return quickBooksCompanyRepository.save(company);
    }
    
    /**
     * Set a company as active (deactivates others in the same environment)
     */
    @Transactional
    public QuickBooksCompany setActiveCompany(Long companyId, String environment) {
        // First, set all companies in this environment as inactive
        quickBooksCompanyRepository.setAllInactiveForEnvironment(environment);
        
        // Then set the specified company as active
        quickBooksCompanyRepository.setActiveById(companyId);
        
        // Return the updated company
        return quickBooksCompanyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with ID: " + companyId));
    }
    
    /**
     * Create a sandbox company (this would typically call QuickBooks API)
     */
    @Transactional
    public QuickBooksCompany createSandboxCompany(String companyName) {
        // In a real implementation, this would call the QuickBooks API to create a sandbox company
        // For now, we'll generate a mock company ID
        String mockCompanyId = generateMockCompanyId();
        
        logger.info("Creating sandbox company: {} with ID: {}", companyName, mockCompanyId);
        
        QuickBooksCompany company = new QuickBooksCompany(mockCompanyId, companyName, SANDBOX_ENV, true);
        
        // Set this as the active company for SANDBOX environment
        quickBooksCompanyRepository.setAllInactiveForEnvironment(SANDBOX_ENV);
        company.setIsActive(true);
        
        return quickBooksCompanyRepository.save(company);
    }
    
    /**
     * Delete a company
     */
    @Transactional
    public void deleteCompany(Long companyId) {
        if (!quickBooksCompanyRepository.existsById(companyId)) {
            throw new IllegalArgumentException("Company not found with ID: " + companyId);
        }
        quickBooksCompanyRepository.deleteById(companyId);
    }
    
    /**
     * Get company by ID
     */
    public Optional<QuickBooksCompany> getCompanyById(Long companyId) {
        return quickBooksCompanyRepository.findById(companyId);
    }
    
    /**
     * Get company by company ID and environment
     */
    public Optional<QuickBooksCompany> getCompanyByCompanyIdAndEnvironment(String companyId, String environment) {
        return quickBooksCompanyRepository.findByCompanyIdAndEnvironment(companyId, environment);
    }
    
    /**
     * Generate a mock company ID for sandbox testing
     * In production, this would come from QuickBooks API
     */
    private String generateMockCompanyId() {
        // Generate a random 16-digit number similar to real QuickBooks company IDs
        long randomId = (long) (Math.random() * 9000000000000000L) + 1000000000000000L;
        return String.valueOf(randomId);
    }
    
    /**
     * Get all companies
     */
    public List<QuickBooksCompany> getAllCompanies() {
        return quickBooksCompanyRepository.findAllByOrderByEnvironmentAscCreatedAtDesc();
    }
}
