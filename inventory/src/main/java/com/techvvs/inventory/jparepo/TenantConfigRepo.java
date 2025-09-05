package com.techvvs.inventory.jparepo;

import com.techvvs.inventory.model.TenantConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantConfigRepo extends JpaRepository<TenantConfig, UUID> {

    List<TenantConfig> findByTenantId(UUID tenantId);

    @Query("SELECT tc FROM TenantConfig tc WHERE tc.tenant.id = :tenantId")
    List<TenantConfig> findByTenant(@Param("tenantId") UUID tenantId);

    @Query("SELECT tc FROM TenantConfig tc WHERE tc.tenant.id = :tenantId AND tc.configKey = :configKey")
    Optional<TenantConfig> findByTenantAndConfigKey(@Param("tenantId") UUID tenantId, @Param("configKey") String configKey);

    @Query("SELECT tc FROM TenantConfig tc WHERE tc.configKey = :configKey")
    List<TenantConfig> findByConfigKey(@Param("configKey") String configKey);

    @Query("SELECT tc FROM TenantConfig tc WHERE tc.tenant.tenantName = :tenantName AND tc.configKey = :configKey")
    Optional<TenantConfig> findByTenantNameAndConfigKey(@Param("tenantName") String tenantName, @Param("configKey") String configKey);

    @Query("SELECT tc FROM TenantConfig tc WHERE tc.tenant.tenantName = :tenantName")
    List<TenantConfig> findByTenantName(@Param("tenantName") String tenantName);
}



