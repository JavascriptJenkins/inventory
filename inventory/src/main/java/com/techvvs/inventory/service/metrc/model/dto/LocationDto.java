package com.techvvs.inventory.service.metrc.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LocationDto {

    @JsonProperty("Id")
    private Long id;

    @JsonProperty("Name")
    private String name;

    @JsonProperty("LocationTypeId")
    private Long locationTypeId;

    @JsonProperty("LocationTypeName")
    private String locationTypeName;

    @JsonProperty("ForPlantBatches")
    private boolean forPlantBatches;

    @JsonProperty("ForPlants")
    private boolean forPlants;

    @JsonProperty("ForHarvests")
    private boolean forHarvests;

    @JsonProperty("ForPackages")
    private boolean forPackages;

    @JsonProperty("LicenseNumber")
    private String licenseNumber;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Long getLocationTypeId() { return locationTypeId; }
    public void setLocationTypeId(Long locationTypeId) { this.locationTypeId = locationTypeId; }
    
    public String getLocationTypeName() { return locationTypeName; }
    public void setLocationTypeName(String locationTypeName) { this.locationTypeName = locationTypeName; }
    
    public boolean isForPlantBatches() { return forPlantBatches; }
    public void setForPlantBatches(boolean forPlantBatches) { this.forPlantBatches = forPlantBatches; }
    
    public boolean isForPlants() { return forPlants; }
    public void setForPlants(boolean forPlants) { this.forPlants = forPlants; }
    
    public boolean isForHarvests() { return forHarvests; }
    public void setForHarvests(boolean forHarvests) { this.forHarvests = forHarvests; }
    
    public boolean isForPackages() { return forPackages; }
    public void setForPackages(boolean forPackages) { this.forPackages = forPackages; }
    
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
}
