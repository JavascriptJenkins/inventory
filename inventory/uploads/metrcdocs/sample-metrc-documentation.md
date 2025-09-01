# METRC Cannabis Compliance Documentation

## Packages (CA)

### Server: https://api-ca.metrc.com

### GET /packages/v1/active
Retrieve all active packages for the authenticated user.

**Parameters:**
- `licenseNumber` (string, required): The license number
- `lastModifiedStart` (string, optional): Start date for filtering
- `lastModifiedEnd` (string, optional): End date for filtering

**Response:**
```json
{
  "Id": 12345,
  "PackageId": "1A4FF0100000029000000001",
  "Label": "Package Label",
  "Status": "Active",
  "Item": "Buds",
  "Quantity": 100.0,
  "UnitOfMeasureName": "Grams",
  "UnitOfMeasureAbbreviation": "g",
  "ReceivedDateTime": "2023-01-01T00:00:00.000",
  "ReceivedFromManifestNumber": "M123456789",
  "ReceivedFromFacilityLicenseNumber": "C12-0000001-LIC",
  "ReceivedFromFacilityName": "Source Facility",
  "IsProductionBatch": false,
  "ProductionBatchNumber": null,
  "ProductRequiresRemediation": false,
  "RemediateProduct": false,
  "RemediationDate": null,
  "RemediationMethodName": null,
  "RemediationSteps": null,
  "RemediationOilsApplied": null,
  "LocationName": "Main Room",
  "LocationTypeName": "Room",
  "LocationQuarantined": false,
  "ShippedDateTime": null,
  "ShippedToManifestNumber": null,
  "ShippedToFacilityLicenseNumber": null,
  "ShippedToFacilityName": null,
  "FinishedDate": null,
  "IsOnHold": false,
  "ArchivedDate": null,
  "LastModified": "2023-01-01T00:00:00.000"
}
```

**Source:** https://api-ca.metrc.com/Documentation/Packages

### PUT /packages/v2/unfinish
Unfinish a package that has been finished.

**Parameters:**
- `id` (integer, required): The package ID

**Response:**
```json
{
  "success": true
}
```

**Source:** https://api-ca.metrc.com/Documentation/Packages

## Transfers (CA)

### Server: https://api-ca.metrc.com

### GET /transfers/v1/incoming
Retrieve incoming transfers for the authenticated user.

**Parameters:**
- `licenseNumber` (string, required): The license number
- `lastModifiedStart` (string, optional): Start date for filtering
- `lastModifiedEnd` (string, optional): End date for filtering

**Response:**
```json
{
  "Id": 12345,
  "ManifestNumber": "M123456789",
  "ShipperLicenseNumber": "C12-0000001-LIC",
  "ShipperName": "Source Facility",
  "ShipperMainPhone": "555-123-4567",
  "ShipperAddress1": "123 Main St",
  "ShipperAddress2": "Suite 100",
  "ShipperAddressCity": "Sacramento",
  "ShipperAddressState": "CA",
  "ShipperAddressZipCode": "95814",
  "TransporterFacilityLicenseNumber": "C12-0000002-LIC",
  "TransporterFacilityName": "Transport Company",
  "TransporterFacilityMainPhone": "555-987-6543",
  "TransporterFacilityAddress1": "456 Transport Ave",
  "TransporterFacilityAddress2": null,
  "TransporterFacilityAddressCity": "Sacramento",
  "TransporterFacilityAddressState": "CA",
  "TransporterFacilityAddressZipCode": "95814",
  "DriverOccupationalLicenseNumber": "DL123456789",
  "DriverName": "John Doe",
  "DriverLicenseNumber": "DL123456789",
  "VehicleMake": "Ford",
  "VehicleModel": "Transit",
  "VehicleLicensePlateNumber": "ABC123",
  "DeliveryCount": 1,
  "ReceivedDeliveryCount": 0,
  "CreatedDateTime": "2023-01-01T00:00:00.000",
  "CreatedByUserName": "system",
  "LastModified": "2023-01-01T00:00:00.000",
  "Deliveries": [
    {
      "Id": 12345,
      "ManifestNumber": "M123456789",
      "RecipientLicenseNumber": "C12-0000003-LIC",
      "RecipientName": "Destination Facility",
      "RecipientMainPhone": "555-456-7890",
      "RecipientAddress1": "789 Destination Blvd",
      "RecipientAddress2": null,
      "RecipientAddressCity": "Sacramento",
      "RecipientAddressState": "CA",
      "RecipientAddressZipCode": "95814",
      "PlannedRoute": "Direct route",
      "EstimatedDepartureDateTime": "2023-01-01T08:00:00.000",
      "EstimatedArrivalDateTime": "2023-01-01T10:00:00.000",
      "ActualArrivalDateTime": null,
      "DeliveryPackageCount": 5,
      "DeliveryReceivedPackageCount": 0,
      "ReceivedDateTime": null,
      "ReceivedByUserName": null,
      "IsRejected": false,
      "RejectedDateTime": null,
      "RejectedReason": null,
      "RejectedByUserName": null,
      "CreatedDateTime": "2023-01-01T00:00:00.000",
      "CreatedByUserName": "system",
      "LastModified": "2023-01-01T00:00:00.000",
      "Packages": [
        {
          "Id": 12345,
          "PackageId": "1A4FF0100000029000000001",
          "Label": "Package Label",
          "Status": "Active",
          "Item": "Buds",
          "Quantity": 100.0,
          "UnitOfMeasureName": "Grams",
          "UnitOfMeasureAbbreviation": "g",
          "ReceivedDateTime": "2023-01-01T00:00:00.000",
          "ReceivedFromManifestNumber": "M123456789",
          "ReceivedFromFacilityLicenseNumber": "C12-0000001-LIC",
          "ReceivedFromFacilityName": "Source Facility",
          "IsProductionBatch": false,
          "ProductionBatchNumber": null,
          "ProductRequiresRemediation": false,
          "RemediateProduct": false,
          "RemediationDate": null,
          "RemediationMethodName": null,
          "RemediationSteps": null,
          "RemediationOilsApplied": null,
          "LocationName": "Main Room",
          "LocationTypeName": "Room",
          "LocationQuarantined": false,
          "ShippedDateTime": null,
          "ShippedToManifestNumber": null,
          "ShippedToFacilityLicenseNumber": null,
          "ShippedToFacilityName": null,
          "FinishedDate": null,
          "IsOnHold": false,
          "ArchivedDate": null,
          "LastModified": "2023-01-01T00:00:00.000"
        }
      ]
    }
  ]
}
```

**Source:** https://api-ca.metrc.com/Documentation/Transfers

## Sales (CA)

### Server: https://api-ca.metrc.com

### GET /sales/v1/receipts
Retrieve sales receipts for the authenticated user.

**Parameters:**
- `licenseNumber` (string, required): The license number
- `lastModifiedStart` (string, optional): Start date for filtering
- `lastModifiedEnd` (string, optional): End date for filtering

**Response:**
```json
{
  "Id": 12345,
  "ReceiptNumber": "R123456789",
  "SalesDateTime": "2023-01-01T12:00:00.000",
  "SalesCustomerType": "Consumer",
  "PatientLicenseNumber": null,
  "CaregiverLicenseNumber": null,
  "IdentificationMethod": "Drivers License",
  "TotalPackageCount": 2,
  "TotalPrice": 150.00,
  "Transactions": [
    {
      "Id": 12345,
      "PackageId": "1A4FF0100000029000000001",
      "PackageLabel": "Package Label",
      "ItemName": "Buds",
      "QuantitySold": 10.0,
      "UnitOfMeasureName": "Grams",
      "UnitOfMeasureAbbreviation": "g",
      "TotalAmount": 75.00,
      "SalesDateTime": "2023-01-01T12:00:00.000",
      "LastModified": "2023-01-01T12:00:00.000"
    }
  ],
  "LastModified": "2023-01-01T12:00:00.000"
}
```

**Source:** https://api-ca.metrc.com/Documentation/Sales

## Compliance Requirements

### Testing Requirements
All cannabis products must be tested for:
- Potency (THC/CBD levels)
- Pesticides
- Heavy metals
- Microbial contaminants
- Residual solvents

### Labeling Requirements
All packages must include:
- Product name
- THC/CBD content
- Net weight
- Batch number
- Testing lab information
- Warning statements

### Record Keeping
Facilities must maintain records for:
- All inventory movements
- Testing results
- Sales transactions
- Transfer manifests
- Employee training records

**Source:** https://api-ca.metrc.com/Documentation/Compliance
