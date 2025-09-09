package com.techvvs.inventory.repository;

import com.techvvs.inventory.model.QuickBooksCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuickBooksCompanyRepository extends JpaRepository<QuickBooksCompany, Long> {
    
    // Find companies by environment
    List<QuickBooksCompany> findByEnvironmentOrderByCreatedAtDesc(String environment);
    
    // Find active company for environment
    Optional<QuickBooksCompany> findByEnvironmentAndIsActiveTrue(String environment);
    
    // Find company by company ID and environment
    Optional<QuickBooksCompany> findByCompanyIdAndEnvironment(String companyId, String environment);
    
    // Check if company exists by company ID and environment
    boolean existsByCompanyIdAndEnvironment(String companyId, String environment);
    
    // Find all companies ordered by environment and creation date
    List<QuickBooksCompany> findAllByOrderByEnvironmentAscCreatedAtDesc();
    
    // Set all companies as inactive for an environment
    @Modifying
    @Transactional
    @Query("UPDATE QuickBooksCompany q SET q.isActive = false WHERE q.environment = :environment")
    void setAllInactiveForEnvironment(@Param("environment") String environment);
    
    // Set a specific company as active
    @Modifying
    @Transactional
    @Query("UPDATE QuickBooksCompany q SET q.isActive = true WHERE q.id = :id")
    void setActiveById(@Param("id") Long id);
    
    // Find sandbox created companies
    List<QuickBooksCompany> findByEnvironmentAndIsSandboxCreatedTrue(String environment);
}
