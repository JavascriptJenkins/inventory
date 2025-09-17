package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantRepo extends JpaRepository<Tenant, UUID> {

    Optional<Tenant> findByTenantName(String tenantName);

    List<Tenant> findByStatus(String status);

    List<Tenant> findBySubscriptionTier(String subscriptionTier);

    @Query("SELECT t FROM Tenant t WHERE t.billingEmail = :email")
    List<Tenant> findByBillingEmail(@Param("email") String email);

    @Query("SELECT t FROM Tenant t WHERE t.domainName = :domainName")
    Optional<Tenant> findByDomainName(@Param("domainName") String domainName);

    @Query("SELECT t FROM Tenant t WHERE t.tenantName LIKE %:name%")
    List<Tenant> findByTenantNameContaining(@Param("name") String name);

    @Query("SELECT t FROM Tenant t WHERE t.status IN :statuses")
    List<Tenant> findByStatusIn(@Param("statuses") List<String> statuses);

    // Query methods for deployment scheduling
    @Query("SELECT t FROM Tenant t WHERE t.deployflag = 0")
    List<Tenant> findTenantsNeedingDeployment();

    @Query("SELECT t FROM Tenant t WHERE t.lastDeployed IS NULL")
    List<Tenant> findTenantsNeverDeployed();

    // Find tenants that need deployment but exclude those already in progress (deployflag = 2)
    @Query("SELECT t FROM Tenant t WHERE (t.deployflag = 0 OR t.lastDeployed IS NULL) AND t.deployflag != 2")
    List<Tenant> findTenantsNeedingDeploymentOrNeverDeployed();

    // Find tenants marked for deletion
    @Query("SELECT t FROM Tenant t WHERE t.deleteFlag = :deleteFlag")
    List<Tenant> findByDeleteFlag(@Param("deleteFlag") Integer deleteFlag);

    // Find tenant by both tenant name and domain name combination
    @Query("SELECT t FROM Tenant t WHERE t.tenantName = :tenantName AND t.domainName = :domainName")
    Optional<Tenant> findByTenantNameAndDomainName(@Param("tenantName") String tenantName, @Param("domainName") String domainName);
}
