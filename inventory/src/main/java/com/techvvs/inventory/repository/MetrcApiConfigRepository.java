package com.techvvs.inventory.repository;

import com.techvvs.inventory.model.MetrcApiConfigVO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetrcApiConfigRepository extends JpaRepository<MetrcApiConfigVO, Long> {
    
    // Find the active configuration (assuming there's only one active config)
    Optional<MetrcApiConfigVO> findFirstByOrderByUpdatedAtDesc();
    
    // Check if configuration exists
    boolean existsById(Long id);
}
