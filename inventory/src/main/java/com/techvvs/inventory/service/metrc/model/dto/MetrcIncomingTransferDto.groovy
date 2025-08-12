package com.techvvs.inventory.service.metrc.model.dto

import java.time.LocalDateTime

class MetrcIncomingTransferDto {
    Long id
    String manifestNumber
    String shipperFacilityLicenseNumber
    String shipperFacilityName
    String recipientFacilityLicenseNumber
    String recipientFacilityName
    String driverName
    String driverLicenseNumber
    String driverPhoneNumber
    String vehicleType
    String vehicleLicensePlateNumber
    String transferTypeName
    String transporterFacilityLicenseNumber
    String transporterFacilityName
    String transporterFacilityContactName
    String transporterFacilityContactPhoneNumber
    String transporterFacilityContactEmailAddress
    List<MetrcPackageDto> packages
    List<MetrcTransporterDto> transporters
    LocalDateTime estimatedDepartureDateTime
    LocalDateTime estimatedArrivalDateTime
    LocalDateTime deliveryDateTime
    Long deliveryId
    Boolean isExport
    String externalExportId
    String note
    LocalDateTime createdDateTime
    LocalDateTime lastModifiedDateTime
}
