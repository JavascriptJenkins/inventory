package com.techvvs.inventory.service.metrc.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MetrcFacilityDto {

    @JsonProperty("FacilityId")
     int facilityId;

    @JsonProperty("HireDate")
     String hireDate;

    @JsonProperty("IsOwner")
     boolean isOwner;

    @JsonProperty("IsManager")
     boolean isManager;

    @JsonProperty("IsFinancialContact")
     boolean isFinancialContact;

    @JsonProperty("Email")
     String email;

    @JsonProperty("Occupations")
     List<String> occupations;

    @JsonProperty("Name")
     String name;

    @JsonProperty("Alias")
     String alias;

    @JsonProperty("DisplayName")
     String displayName;

    @JsonProperty("CredentialedDate")
     String credentialedDate;

    @JsonProperty("SupportActivationDate")
     String supportActivationDate;

    @JsonProperty("SupportExpirationDate")
     String supportExpirationDate;

    @JsonProperty("FacilityType")
     FacilityType facilityType;

    @JsonProperty("License")
     License license;

    // Nested classes for FacilityType and License
    public static class FacilityType {

        @JsonProperty("IsMedical")
         boolean isMedical;

        @JsonProperty("IsRetail")
         boolean isRetail;

        @JsonProperty("IsHemp")
         boolean isHemp;

        @JsonProperty("RestrictHarvestPlantRestoreTimeHours")
         Integer restrictHarvestPlantRestoreTimeHours;

        @JsonProperty("TotalMemberPatientsAllowed")
         Integer totalMemberPatientsAllowed;

        @JsonProperty("RestrictWholesalePriceEditDays")
         Integer restrictWholesalePriceEditDays;

        @JsonProperty("RestrictPlantBatchAdjustmentTimeHours")
         Integer restrictPlantBatchAdjustmentTimeHours;

        @JsonProperty("CanGrowPlants")
         boolean canGrowPlants;

        @JsonProperty("CanCreateOpeningBalancePlantBatches")
         boolean canCreateOpeningBalancePlantBatches;

        @JsonProperty("CanClonePlantBatches")
         boolean canClonePlantBatches;

        @JsonProperty("CanTagPlantBatches")
         boolean canTagPlantBatches;

        @JsonProperty("CanAssignLocationsToPlantBatches")
         boolean canAssignLocationsToPlantBatches;

        @JsonProperty("PlantsRequirePatientAffiliation")
         boolean plantsRequirePatientAffiliation;

        @JsonProperty("PlantBatchesCanContainMotherPlants")
         boolean plantBatchesCanContainMotherPlants;

        @JsonProperty("CanUpdatePlantStrains")
         boolean canUpdatePlantStrains;

        @JsonProperty("CanTrackVegetativePlants")
         boolean canTrackVegetativePlants;

        @JsonProperty("CanCreateImmaturePlantPackagesFromPlants")
         boolean canCreateImmaturePlantPackagesFromPlants;

        @JsonProperty("CanPackageVegetativePlants")
         boolean canPackageVegetativePlants;

        @JsonProperty("CanPackageWaste")
         boolean canPackageWaste;

        @JsonProperty("CanReportHarvestSchedules")
         boolean canReportHarvestSchedules;

        @JsonProperty("CanSubmitHarvestsForTesting")
         boolean canSubmitHarvestsForTesting;

        @JsonProperty("CanRequireHarvestSampleLabTestBatches")
         boolean canRequireHarvestSampleLabTestBatches;

        @JsonProperty("CanReportStrainProperties")
         boolean canReportStrainProperties;

        @JsonProperty("CanCreateOpeningBalancePackages")
         boolean canCreateOpeningBalancePackages;

        @JsonProperty("CanCreateDerivedPackages")
         boolean canCreateDerivedPackages;

        @JsonProperty("CanAssignLocationsToPackages")
         boolean canAssignLocationsToPackages;

        @JsonProperty("CanUpdateLocationsOnPackages")
         boolean canUpdateLocationsOnPackages;

        @JsonProperty("PackagesRequirePatientAffiliation")
         boolean packagesRequirePatientAffiliation;

        @JsonProperty("CanCreateTradeSamplePackages")
         boolean canCreateTradeSamplePackages;

        @JsonProperty("CanDonatePackages")
         boolean canDonatePackages;

        @JsonProperty("CanSubmitPackagesForTesting")
         boolean canSubmitPackagesForTesting;

        @JsonProperty("CanCreateProcessValidationPackages")
         boolean canCreateProcessValidationPackages;

        @JsonProperty("CanRequirePackageSampleLabTestBatches")
         boolean canRequirePackageSampleLabTestBatches;

        @JsonProperty("CanRequestProductRemediation")
         boolean canRequestProductRemediation;

        @JsonProperty("CanRemediatePackagesWithFailedLabResults")
         boolean canRemediatePackagesWithFailedLabResults;

        @JsonProperty("CanRequestProductDecontamination")
         boolean canRequestProductDecontamination;

        @JsonProperty("CanDecontaminatePackagesWithFailedLabResults")
         boolean canDecontaminatePackagesWithFailedLabResults;

        @JsonProperty("CanInfuseProducts")
         boolean canInfuseProducts;

        @JsonProperty("CanRecordProcessingJobs")
         boolean canRecordProcessingJobs;

        @JsonProperty("CanRecordProductForDestruction")
         boolean canRecordProductForDestruction;

        @JsonProperty("CanDestroyProduct")
         boolean canDestroyProduct;

        @JsonProperty("CanTestPackages")
         boolean canTestPackages;

        @JsonProperty("TestsRequireLabSample")
         boolean testsRequireLabSample;

        @JsonProperty("CanTransferFromExternalFacilities")
         boolean canTransferFromExternalFacilities;

        @JsonProperty("CanSellToConsumers")
         boolean canSellToConsumers;

        @JsonProperty("CanSellToPatients")
         boolean canSellToPatients;

        @JsonProperty("CanSellToExternalPatients")
         boolean canSellToExternalPatients;

        @JsonProperty("CanSellToCaregivers")
         boolean canSellToCaregivers;

        @JsonProperty("AdvancedSales")
         boolean advancedSales;

        @JsonProperty("SalesRequirePatientNumber")
         boolean salesRequirePatientNumber;

        @JsonProperty("SalesRequireExternalPatientNumber")
         boolean salesRequireExternalPatientNumber;

        @JsonProperty("SalesRequireExternalPatientIdentificationMethod")
         boolean salesRequireExternalPatientIdentificationMethod;

        @JsonProperty("SalesRequireCaregiverNumber")
         boolean salesRequireCaregiverNumber;

        @JsonProperty("SalesRequireCaregiverPatientNumber")
         boolean salesRequireCaregiverPatientNumber;

        @JsonProperty("CanDeliverSalesToConsumers")
         boolean canDeliverSalesToConsumers;

        @JsonProperty("SalesDeliveryAllowPlannedRoute")
         boolean salesDeliveryAllowPlannedRoute;

        @JsonProperty("SalesDeliveryAllowAddress")
         boolean salesDeliveryAllowAddress;

        @JsonProperty("SalesDeliveryAllowCity")
         boolean salesDeliveryAllowCity;

        @JsonProperty("SalesDeliveryAllowState")
         boolean salesDeliveryAllowState;

        @JsonProperty("SalesDeliveryAllowCounty")
         boolean salesDeliveryAllowCounty;

        @JsonProperty("SalesDeliveryAllowZip")
         boolean salesDeliveryAllowZip;

        @JsonProperty("SalesDeliveryRequireZone")
         boolean salesDeliveryRequireZone;

        @JsonProperty("EnableExternalIdentifier")
         boolean enableExternalIdentifier;

        @JsonProperty("SalesDeliveryRequireConsumerId")
         boolean salesDeliveryRequireConsumerId;

        @JsonProperty("CanDeliverSalesToPatients")
         boolean canDeliverSalesToPatients;

        @JsonProperty("SalesDeliveryRequirePatientNumber")
         boolean salesDeliveryRequirePatientNumber;

        @JsonProperty("SalesDeliveryRequireRecipientName")
         boolean salesDeliveryRequireRecipientName;

        @JsonProperty("IsSalesDeliveryHub")
         boolean isSalesDeliveryHub;

        @JsonProperty("CanHaveMemberPatients")
         boolean canHaveMemberPatients;

        @JsonProperty("CanReportPatientCheckIns")
         boolean canReportPatientCheckIns;

        @JsonProperty("CanSpecifyPatientSalesLimitExemption")
         boolean canSpecifyPatientSalesLimitExemption;

        @JsonProperty("CanReportPatientsAdverseResponses")
         boolean canReportPatientsAdverseResponses;

        @JsonProperty("RetailerDelivery")
         boolean retailerDelivery;

        @JsonProperty("RetailerDeliveryAllowTradeSamples")
         boolean retailerDeliveryAllowTradeSamples;

        @JsonProperty("RetailerDeliveryAllowDonations")
         boolean retailerDeliveryAllowDonations;

        @JsonProperty("RetailerDeliveryRequirePrice")
         boolean retailerDeliveryRequirePrice;

        @JsonProperty("RetailerDeliveryAllowPartialPackages")
         boolean retailerDeliveryAllowPartialPackages;

        @JsonProperty("CanCreatePartialPackages")
         boolean canCreatePartialPackages;

        @JsonProperty("CanAdjustSourcePackagesWithPartials")
         boolean canAdjustSourcePackagesWithPartials;

        @JsonProperty("CanReportOperationalExceptions")
         boolean canReportOperationalExceptions;

        @JsonProperty("CanReportAdulteration")
         boolean canReportAdulteration;

        @JsonProperty("CanDownloadProductLabel")
         boolean canDownloadProductLabel;

        @JsonProperty("CanReceiveAssociateProductLabel")
         boolean canReceiveAssociateProductLabel;

        @JsonProperty("CanUseComplianceLabel")
         boolean canUseComplianceLabel;

        @JsonProperty("CanViewSourcePackages")
         boolean canViewSourcePackages;

        @JsonProperty("EnableSublocations")
         boolean enableSublocations;

        @JsonProperty("TaxExemptReportingFeesFacilityType")
         boolean taxExemptReportingFeesFacilityType;

        @JsonProperty("TaxExemptTagOrdersFacilityType")
         boolean taxExemptTagOrdersFacilityType;

        @JsonProperty("CanAccessCatalog")
         boolean canAccessCatalog;

        // Getters and Setters for this nested class
    }

    public static class License {
        @JsonProperty("Number")
         String number;

        @JsonProperty("StartDate")
         String startDate;

        @JsonProperty("EndDate")
         String endDate;

        @JsonProperty("LicenseType")
         String licenseType;

        // Getters and Setters for this nested class
    }

    // Getters and Setters for main DTO fields
}
