package com.techvvs.inventory.service.kubernetes;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing Kubernetes resources including namespaces and RBAC
 * Handles creation of tenant namespaces and associated RBAC configurations
 */
@Service
public class KubernetesService {

    private static final Logger logger = LoggerFactory.getLogger(KubernetesService.class);

    @Value("${kubernetes.kubeconfig.path:static/kubernetes/tulip-sandbox-kubeconfig.yaml}")
    private String kubeconfigPath;

    private ApiClient apiClient;
    private CoreV1Api coreV1Api;
    private RbacAuthorizationV1Api rbacApi;

    @PostConstruct
    public void initializeKubernetesClient() {
        try {
            logger.info("Initializing Kubernetes client with kubeconfig: {}", kubeconfigPath);
            
            // Load kubeconfig from classpath
            ClassPathResource resource = new ClassPathResource(kubeconfigPath);
            File kubeconfigFile = resource.getFile();
            
            // Create API client from kubeconfig
            apiClient = Config.fromConfig(kubeconfigFile.getAbsolutePath());
            Configuration.setDefaultApiClient(apiClient);
            
            // Initialize API clients
            coreV1Api = new CoreV1Api(apiClient);
            rbacApi = new RbacAuthorizationV1Api(apiClient);
            
            logger.info("Successfully initialized Kubernetes client");
            
        } catch (IOException e) {
            logger.error("Failed to initialize Kubernetes client", e);
            throw new RuntimeException("Failed to initialize Kubernetes client", e);
        }
    }

    /**
     * Creates a namespace for a tenant if it doesn't exist
     * 
     * @param tenantName The tenant name to create namespace for
     * @return true if successful, false otherwise
     */
    public boolean createNamespace(String tenantName) {
        try {
            String namespaceName = "tenant-" + tenantName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
            
            logger.info("Creating Kubernetes namespace: {}", namespaceName);

            // Check if namespace already exists
            try {
                V1Namespace existingNamespace = coreV1Api.readNamespace(namespaceName, null);
                if (existingNamespace != null) {
                    logger.info("Namespace '{}' already exists, skipping creation", namespaceName);
                    return true;
                }
            } catch (ApiException e) {
                if (e.getCode() == 404) {
                    // Namespace doesn't exist, we can create it
                    logger.info("Namespace '{}' does not exist, will create it", namespaceName);
                } else {
                    logger.error("Error checking if namespace exists", e);
                    return false;
                }
            }

            // Create namespace
            V1Namespace namespace = new V1Namespace();
            V1ObjectMeta metadata = new V1ObjectMeta();
            metadata.setName(namespaceName);
            
            // Add labels for tenant identification
            Map<String, String> labels = new HashMap<>();
            labels.put("tenant", tenantName);
            labels.put("managed-by", "inventory-system");
            labels.put("environment", "sandbox");
            metadata.setLabels(labels);
            
            namespace.setMetadata(metadata);
            
            V1Namespace createdNamespace = coreV1Api.createNamespace(namespace, null, null, null, null);
            
            if (createdNamespace != null) {
                logger.info("Successfully created namespace: {}", namespaceName);
                return true;
            } else {
                logger.error("Failed to create namespace: {}", namespaceName);
                return false;
            }

        } catch (ApiException e) {
            logger.error("API error creating namespace for tenant: {}", tenantName, e);
            return false;
        } catch (Exception e) {
            logger.error("Error creating namespace for tenant: {}", tenantName, e);
            return false;
        }
    }

    /**
     * Creates RBAC configuration for a tenant namespace
     * 
     * @param tenantName The tenant name to create RBAC for
     * @return true if successful, false otherwise
     */
    public boolean createRBACConfiguration(String tenantName) {
        try {
            String namespaceName = "tenant-" + tenantName.toLowerCase().replaceAll("[^a-z0-9-]", "-");
            String serviceAccountName = tenantName + "-admin";
            String roleName = tenantName + "-admin-role";
            String roleBindingName = tenantName + "-admin-binding";
            
            logger.info("Creating RBAC configuration for namespace: {}", namespaceName);

            // 1. Create ServiceAccount
            boolean serviceAccountCreated = createServiceAccount(namespaceName, serviceAccountName);
            if (!serviceAccountCreated) {
                logger.error("Failed to create service account for tenant: {}", tenantName);
                return false;
            }

            // 2. Create Role with admin permissions
            boolean roleCreated = createAdminRole(namespaceName, roleName);
            if (!roleCreated) {
                logger.error("Failed to create role for tenant: {}", tenantName);
                return false;
            }

            // 3. Create RoleBinding
            boolean roleBindingCreated = createRoleBinding(namespaceName, roleBindingName, serviceAccountName, roleName);
            if (!roleBindingCreated) {
                logger.error("Failed to create role binding for tenant: {}", tenantName);
                return false;
            }

            logger.info("Successfully created RBAC configuration for tenant: {}", tenantName);
            return true;

        } catch (Exception e) {
            logger.error("Error creating RBAC configuration for tenant: {}", tenantName, e);
            return false;
        }
    }

    /**
     * Creates a ServiceAccount in the specified namespace
     */
    private boolean createServiceAccount(String namespaceName, String serviceAccountName) {
        try {
            // Check if service account already exists
            try {
                V1ServiceAccount existingSA = coreV1Api.readNamespacedServiceAccount(serviceAccountName, namespaceName, null);
                if (existingSA != null) {
                    logger.info("ServiceAccount '{}' already exists in namespace '{}'", serviceAccountName, namespaceName);
                    return true;
                }
            } catch (ApiException e) {
                if (e.getCode() != 404) {
                    logger.error("Error checking if service account exists", e);
                    return false;
                }
            }

            // Create ServiceAccount
            V1ServiceAccount serviceAccount = new V1ServiceAccount();
            V1ObjectMeta metadata = new V1ObjectMeta();
            metadata.setName(serviceAccountName);
            metadata.setNamespace(namespaceName);
            
            // Add labels
            Map<String, String> labels = new HashMap<>();
            labels.put("tenant", namespaceName.replace("tenant-", ""));
            labels.put("managed-by", "inventory-system");
            metadata.setLabels(labels);
            
            serviceAccount.setMetadata(metadata);
            
            V1ServiceAccount createdSA = coreV1Api.createNamespacedServiceAccount(namespaceName, serviceAccount, null, null, null, null);
            
            if (createdSA != null) {
                logger.info("Successfully created ServiceAccount: {} in namespace: {}", serviceAccountName, namespaceName);
                return true;
            } else {
                logger.error("Failed to create ServiceAccount: {} in namespace: {}", serviceAccountName, namespaceName);
                return false;
            }

        } catch (ApiException e) {
            logger.error("API error creating ServiceAccount: {} in namespace: {}", serviceAccountName, namespaceName, e);
            return false;
        }
    }

    /**
     * Creates an admin Role in the specified namespace
     */
    private boolean createAdminRole(String namespaceName, String roleName) {
        try {
            // Check if role already exists
            try {
                V1Role existingRole = rbacApi.readNamespacedRole(roleName, namespaceName, null);
                if (existingRole != null) {
                    logger.info("Role '{}' already exists in namespace '{}'", roleName, namespaceName);
                    return true;
                }
            } catch (ApiException e) {
                if (e.getCode() != 404) {
                    logger.error("Error checking if role exists", e);
                    return false;
                }
            }

            // Create Role with admin permissions
            V1Role role = new V1Role();
            V1ObjectMeta metadata = new V1ObjectMeta();
            metadata.setName(roleName);
            metadata.setNamespace(namespaceName);
            
            // Add labels
            Map<String, String> labels = new HashMap<>();
            labels.put("tenant", namespaceName.replace("tenant-", ""));
            labels.put("managed-by", "inventory-system");
            metadata.setLabels(labels);
            
            role.setMetadata(metadata);

            // Define admin rules
            V1PolicyRule adminRule = new V1PolicyRule();
            adminRule.addApiGroupsItem("*");
            adminRule.addResourcesItem("*");
            adminRule.addVerbsItem("*");
            
            role.addRulesItem(adminRule);
            
            V1Role createdRole = rbacApi.createNamespacedRole(namespaceName, role, null, null, null, null);
            
            if (createdRole != null) {
                logger.info("Successfully created Role: {} in namespace: {}", roleName, namespaceName);
                return true;
            } else {
                logger.error("Failed to create Role: {} in namespace: {}", roleName, namespaceName);
                return false;
            }

        } catch (ApiException e) {
            logger.error("API error creating Role: {} in namespace: {}", roleName, namespaceName, e);
            return false;
        }
    }

    /**
     * Creates a RoleBinding in the specified namespace
     */
    private boolean createRoleBinding(String namespaceName, String roleBindingName, String serviceAccountName, String roleName) {
        try {
            // Check if role binding already exists
            try {
                V1RoleBinding existingRB = rbacApi.readNamespacedRoleBinding(roleBindingName, namespaceName, null);
                if (existingRB != null) {
                    logger.info("RoleBinding '{}' already exists in namespace '{}'", roleBindingName, namespaceName);
                    return true;
                }
            } catch (ApiException e) {
                if (e.getCode() != 404) {
                    logger.error("Error checking if role binding exists", e);
                    return false;
                }
            }

            // Create RoleBinding
            V1RoleBinding roleBinding = new V1RoleBinding();
            V1ObjectMeta metadata = new V1ObjectMeta();
            metadata.setName(roleBindingName);
            metadata.setNamespace(namespaceName);
            
            // Add labels
            Map<String, String> labels = new HashMap<>();
            labels.put("tenant", namespaceName.replace("tenant-", ""));
            labels.put("managed-by", "inventory-system");
            metadata.setLabels(labels);
            
            roleBinding.setMetadata(metadata);

            // Set role reference
            V1RoleRef roleRef = new V1RoleRef();
            roleRef.setApiGroup("rbac.authorization.k8s.io");
            roleRef.setKind("Role");
            roleRef.setName(roleName);
            roleBinding.setRoleRef(roleRef);

            // Set subjects (ServiceAccount)
            V1Subject subject = new V1Subject();
            subject.setKind("ServiceAccount");
            subject.setName(serviceAccountName);
            subject.setNamespace(namespaceName);
            roleBinding.addSubjectsItem(subject);
            
            V1RoleBinding createdRB = rbacApi.createNamespacedRoleBinding(namespaceName, roleBinding, null, null, null, null);
            
            if (createdRB != null) {
                logger.info("Successfully created RoleBinding: {} in namespace: {}", roleBindingName, namespaceName);
                return true;
            } else {
                logger.error("Failed to create RoleBinding: {} in namespace: {}", roleBindingName, namespaceName);
                return false;
            }

        } catch (ApiException e) {
            logger.error("API error creating RoleBinding: {} in namespace: {}", roleBindingName, namespaceName, e);
            return false;
        }
    }

    /**
     * Comprehensive tenant Kubernetes setup that creates namespace and RBAC
     * 
     * @param tenantName The tenant name to set up
     * @return true if all operations successful, false otherwise
     */
    public boolean setupTenantKubernetes(String tenantName) {
        logger.info("Starting Kubernetes setup for tenant: {}", tenantName);
        
        boolean namespaceSuccess = createNamespace(tenantName);
        boolean rbacSuccess = createRBACConfiguration(tenantName);
        
        boolean overallSuccess = namespaceSuccess && rbacSuccess;
        
        if (overallSuccess) {
            logger.info("Successfully set up Kubernetes resources for tenant: {}", tenantName);
        } else {
            logger.warn("Partial Kubernetes setup for tenant: {} - Namespace: {}, RBAC: {}", 
                       tenantName, namespaceSuccess, rbacSuccess);
        }
        
        return overallSuccess;
    }

    /**
     * Checks if the service is properly configured
     * 
     * @return true if configured, false otherwise
     */
    public boolean isConfigured() {
        return apiClient != null && coreV1Api != null && rbacApi != null;
    }
}
