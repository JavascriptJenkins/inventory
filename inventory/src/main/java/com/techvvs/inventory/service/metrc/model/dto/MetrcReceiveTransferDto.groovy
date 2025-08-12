package com.techvvs.inventory.service.metrc.model.dto

class MetrcReceiveTransferDto {
    Long id
    String recipientFacilityLicenseNumber
    List<MetrcReceivedPackageDto> receivedPackages
}
