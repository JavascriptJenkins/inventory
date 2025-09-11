package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.SystemUserDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemUserRepo extends JpaRepository<SystemUserDAO, Integer> {

        List<SystemUserDAO> findAll();
        SystemUserDAO findByEmail(String email);
        Optional<SystemUserDAO> findById(Integer id);
        
        // Find system users by tenant
        List<SystemUserDAO> findByTenantEntityId(UUID tenantId);
        
        // OAuth-related queries
        SystemUserDAO findByGoogleId(String googleId);
        SystemUserDAO findByOauthEmail(String oauthEmail);
        List<SystemUserDAO> findByOauthProvider(String oauthProvider);
        List<SystemUserDAO> findByOauthLinkedTrue();


       // List<SystemUserDAO> findByOrderByUpvotesDesc();


}
