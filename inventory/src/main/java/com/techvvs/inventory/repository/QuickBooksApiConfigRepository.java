package com.techvvs.inventory.repository;

import com.techvvs.inventory.model.QuickBooksApiConfigVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuickBooksApiConfigRepository extends JpaRepository<QuickBooksApiConfigVO, Long> {
    
    // Find configuration by environment
    Optional<QuickBooksApiConfigVO> findByEnvironment(String environment);
    
    // Check if configuration exists for environment
    boolean existsByEnvironment(String environment);
    
    // Find all configurations ordered by environment
    List<QuickBooksApiConfigVO> findAllByOrderByEnvironment();
    
    // Check if configuration exists
    boolean existsById(Long id);
    
    // Find records with null environment (for migration)
    List<QuickBooksApiConfigVO> findByEnvironmentIsNull();
}
