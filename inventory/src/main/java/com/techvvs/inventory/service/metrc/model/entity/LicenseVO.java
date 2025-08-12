package com.techvvs.inventory.service.metrc.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LicenseVO {

    @JsonProperty("Number")
    private String number;

    @JsonProperty("EmployeeLicenseNumber")
    private String employeeLicenseNumber;

    @JsonProperty("StartDate")
    private String startDate;

    @JsonProperty("EndDate")
    private String endDate;

    @JsonProperty("LicenseType")
    private String licenseType;

    // getters/setters
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public String getEmployeeLicenseNumber() { return employeeLicenseNumber; }
    public void setEmployeeLicenseNumber(String employeeLicenseNumber) { this.employeeLicenseNumber = employeeLicenseNumber; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public String getLicenseType() { return licenseType; }
    public void setLicenseType(String licenseType) { this.licenseType = licenseType; }
}
