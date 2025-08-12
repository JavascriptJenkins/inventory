package com.techvvs.inventory.service.metrc.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EmployeeVO {

    @JsonProperty("FullName")
    private String fullName;

    @JsonProperty("License")
    private LicenseVO license;

    @JsonProperty("IsIndustryAdmin")
    private boolean industryAdmin;

    @JsonProperty("IsOwner")
    private boolean owner;

    @JsonProperty("IsManager")
    private boolean manager;

    // getters/setters
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public LicenseVO getLicense() { return license; }
    public void setLicense(LicenseVO license) { this.license = license; }
    public boolean isIndustryAdmin() { return industryAdmin; }
    public void setIndustryAdmin(boolean industryAdmin) { this.industryAdmin = industryAdmin; }
    public boolean isOwner() { return owner; }
    public void setOwner(boolean owner) { this.owner = owner; }
    public boolean isManager() { return manager; }
    public void setManager(boolean manager) { this.manager = manager; }
}
