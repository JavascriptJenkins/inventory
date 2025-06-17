package com.techvvs.inventory.metrc.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MetrcFacilityDto {

    @JsonProperty("FacilityId")
    private int facilityId;

    @JsonProperty("HireDate")
    private String hireDate;

    @JsonProperty("IsOwner")
    private boolean isOwner;

    @JsonProperty("IsManager")
    private boolean isManager;

    @JsonProperty("IsFinancialContact")
    private boolean isFinancialContact;

    @JsonProperty("Email")
    private String email;

    @JsonProperty("Occupations")
    private List<String> occupations;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Alias")
    private String alias;

    @JsonProperty("DisplayName")
    private String displayName;

    @JsonProperty("CredentialedDate")
    private String credentialedDate;

    @JsonProperty("SupportActivationDate")
    private String supportActivationDate;

    @JsonProperty("SupportExpirationDate")
    private String supportExpirationDate;

    @JsonProperty("FacilityType")
    private FacilityType facilityType;

    @JsonProperty("License")
    private License license;

    // Nested classes for FacilityType and License
    public static class FacilityType {

        @JsonProperty("IsMedical")
        private boolean isMedical;

        @JsonProperty("IsRetail")
        private boolean isRetail;

        @JsonProperty("IsHemp")
        private boolean isHemp;

        @JsonProperty("RestrictHarvestPlantRestoreTimeHours")
        private Integer restrictHarvestPlantRestoreTimeHours;

        @JsonProperty("TotalMemberPatientsAllowed")
        private Integer totalMemberPatientsAllowed;

        @JsonProperty("RestrictWholesalePriceEditDays")
        private Integer restrictWholesalePriceEditDays;

        @JsonProperty("RestrictPlantBatchAdjustmentTimeHours")
        private Integer restrictPlantBatchAdjustmentTimeHours;

        @JsonProperty("CanGrowPlants")
        private boolean canGrowPlants;

        @JsonProperty("CanCreateOpeningBalancePlantBatches")
        private boolean canCreateOpeningBalancePlantBatches;

        @JsonProperty("CanClonePlantBatches")
        private boolean canClonePlantBatches;

        @JsonProperty("CanTagPlantBatches")
        private boolean canTagPlantBatches;

        @JsonProperty("CanAssignLocationsToPlantBatches")
        private boolean canAssignLocationsToPlantBatches;

        @JsonProperty("PlantsRequirePatientAffiliation")
        private boolean plantsRequirePatientAffiliation;

        @JsonProperty("PlantBatchesCanContainMotherPlants")
        private boolean plantBatchesCanContainMotherPlants;

        @JsonProperty("CanUpdatePlantStrains")
        private boolean canUpdatePlantStrains;

        @JsonProperty("CanTrackVegetativePlants")
        private boolean canTrackVegetativePlants;

        @JsonProperty("CanCreateImmaturePlantPackagesFromPlants")
        private boolean canCreateImmaturePlantPackagesFromPlants;

        @JsonProperty("CanPackageVegetativePlants")
        private boolean canPackageVegetativePlants;

        @JsonProperty("CanPackageWaste")
        private boolean canPackageWaste;

        @JsonProperty("CanReportHarvestSchedules")
        private boolean canReportHarvestSchedules;

        @JsonProperty("CanSubmitHarvestsForTesting")
        private boolean canSubmitHarvestsForTesting;

        @JsonProperty("CanRequireHarvestSampleLabTestBatches")
        private boolean canRequireHarvestSampleLabTestBatches;

        @JsonProperty("CanReportStrainProperties")
        private boolean canReportStrainProperties;

        @JsonProperty("CanCreateOpeningBalancePackages")
        private boolean canCreateOpeningBalancePackages;

        @JsonProperty("CanCreateDerivedPackages")
        private boolean canCreateDerivedPackages;

        @JsonProperty("CanAssignLocationsToPackages")
        private boolean canAssignLocationsToPackages;

        @JsonProperty("CanUpdateLocationsOnPackages")
        private boolean canUpdateLocationsOnPackages;

        @JsonProperty("PackagesRequirePatientAffiliation")
        private boolean packagesRequirePatientAffiliation;

        @JsonProperty("CanCreateTradeSamplePackages")
        private boolean canCreateTradeSamplePackages;

        @JsonProperty("CanDonatePackages")
        private boolean canDonatePackages;

        @JsonProperty("CanSubmitPackagesForTesting")
        private boolean canSubmitPackagesForTesting;

        @JsonProperty("CanCreateProcessValidationPackages")
        private boolean canCreateProcessValidationPackages;

        @JsonProperty("CanRequirePackageSampleLabTestBatches")
        private boolean canRequirePackageSampleLabTestBatches;

        @JsonProperty("CanRequestProductRemediation")
        private boolean canRequestProductRemediation;

        @JsonProperty("CanRemediatePackagesWithFailedLabResults")
        private boolean canRemediatePackagesWithFailedLabResults;

        @JsonProperty("CanRequestProductDecontamination")
        private boolean canRequestProductDecontamination;

        @JsonProperty("CanDecontaminatePackagesWithFailedLabResults")
        private boolean canDecontaminatePackagesWithFailedLabResults;

        @JsonProperty("CanInfuseProducts")
        private boolean canInfuseProducts;

        @JsonProperty("CanRecordProcessingJobs")
        private boolean canRecordProcessingJobs;

        @JsonProperty("CanRecordProductForDestruction")
        private boolean canRecordProductForDestruction;

        @JsonProperty("CanDestroyProduct")
        private boolean canDestroyProduct;

        @JsonProperty("CanTestPackages")
        private boolean canTestPackages;

        @JsonProperty("TestsRequireLabSample")
        private boolean testsRequireLabSample;

        @JsonProperty("CanTransferFromExternalFacilities")
        private boolean canTransferFromExternalFacilities;

        @JsonProperty("CanSellToConsumers")
        private boolean canSellToConsumers;

        @JsonProperty("CanSellToPatients")
        private boolean canSellToPatients;

        @JsonProperty("CanSellToExternalPatients")
        private boolean canSellToExternalPatients;

        @JsonProperty("CanSellToCaregivers")
        private boolean canSellToCaregivers;

        @JsonProperty("AdvancedSales")
        private boolean advancedSales;

        @JsonProperty("SalesRequirePatientNumber")
        private boolean salesRequirePatientNumber;

        @JsonProperty("SalesRequireExternalPatientNumber")
        private boolean salesRequireExternalPatientNumber;

        @JsonProperty("SalesRequireExternalPatientIdentificationMethod")
        private boolean salesRequireExternalPatientIdentificationMethod;

        @JsonProperty("SalesRequireCaregiverNumber")
        private boolean salesRequireCaregiverNumber;

        @JsonProperty("SalesRequireCaregiverPatientNumber")
        private boolean salesRequireCaregiverPatientNumber;

        @JsonProperty("CanDeliverSalesToConsumers")
        private boolean canDeliverSalesToConsumers;

        @JsonProperty("SalesDeliveryAllowPlannedRoute")
        private boolean salesDeliveryAllowPlannedRoute;

        @JsonProperty("SalesDeliveryAllowAddress")
        private boolean salesDeliveryAllowAddress;

        @JsonProperty("SalesDeliveryAllowCity")
        private boolean salesDeliveryAllowCity;

        @JsonProperty("SalesDeliveryAllowState")
        private boolean salesDeliveryAllowState;

        @JsonProperty("SalesDeliveryAllowCounty")
        private boolean salesDeliveryAllowCounty;

        @JsonProperty("SalesDeliveryAllowZip")
        private boolean salesDeliveryAllowZip;

        @JsonProperty("SalesDeliveryRequireZone")
        private boolean salesDeliveryRequireZone;

        @JsonProperty("EnableExternalIdentifier")
        private boolean enableExternalIdentifier;

        @JsonProperty("SalesDeliveryRequireConsumerId")
        private boolean salesDeliveryRequireConsumerId;

        @JsonProperty("CanDeliverSalesToPatients")
        private boolean canDeliverSalesToPatients;

        @JsonProperty("SalesDeliveryRequirePatientNumber")
        private boolean salesDeliveryRequirePatientNumber;

        @JsonProperty("SalesDeliveryRequireRecipientName")
        private boolean salesDeliveryRequireRecipientName;

        @JsonProperty("IsSalesDeliveryHub")
        private boolean isSalesDeliveryHub;

        @JsonProperty("CanHaveMemberPatients")
        private boolean canHaveMemberPatients;

        @JsonProperty("CanReportPatientCheckIns")
        private boolean canReportPatientCheckIns;

        @JsonProperty("CanSpecifyPatientSalesLimitExemption")
        private boolean canSpecifyPatientSalesLimitExemption;

        @JsonProperty("CanReportPatientsAdverseResponses")
        private boolean canReportPatientsAdverseResponses;

        @JsonProperty("RetailerDelivery")
        private boolean retailerDelivery;

        @JsonProperty("RetailerDeliveryAllowTradeSamples")
        private boolean retailerDeliveryAllowTradeSamples;

        @JsonProperty("RetailerDeliveryAllowDonations")
        private boolean retailerDeliveryAllowDonations;

        @JsonProperty("RetailerDeliveryRequirePrice")
        private boolean retailerDeliveryRequirePrice;

        @JsonProperty("RetailerDeliveryAllowPartialPackages")
        private boolean retailerDeliveryAllowPartialPackages;

        @JsonProperty("CanCreatePartialPackages")
        private boolean canCreatePartialPackages;

        @JsonProperty("CanAdjustSourcePackagesWithPartials")
        private boolean canAdjustSourcePackagesWithPartials;

        @JsonProperty("CanReportOperationalExceptions")
        private boolean canReportOperationalExceptions;

        @JsonProperty("CanReportAdulteration")
        private boolean canReportAdulteration;

        @JsonProperty("CanDownloadProductLabel")
        private boolean canDownloadProductLabel;

        @JsonProperty("CanReceiveAssociateProductLabel")
        private boolean canReceiveAssociateProductLabel;

        @JsonProperty("CanUseComplianceLabel")
        private boolean canUseComplianceLabel;

        @JsonProperty("CanViewSourcePackages")
        private boolean canViewSourcePackages;

        @JsonProperty("EnableSublocations")
        private boolean enableSublocations;

        @JsonProperty("TaxExemptReportingFeesFacilityType")
        private boolean taxExemptReportingFeesFacilityType;

        @JsonProperty("TaxExemptTagOrdersFacilityType")
        private boolean taxExemptTagOrdersFacilityType;

        @JsonProperty("CanAccessCatalog")
        private boolean canAccessCatalog;

        // Getters and Setters for this nested class
    }

    public static class License {
        @JsonProperty("Number")
        private String number;

        @JsonProperty("StartDate")
        private String startDate;

        @JsonProperty("EndDate")
        private String endDate;

        @JsonProperty("LicenseType")
        private String licenseType;

        // Getters and Setters for this nested class
    }

    // Getters and Setters for main DTO fields
}
