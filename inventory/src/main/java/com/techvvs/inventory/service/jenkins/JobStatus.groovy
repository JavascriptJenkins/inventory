package com.techvvs.inventory.service.jenkins

import groovy.transform.builder.Builder;

@Builder
public class JobStatus {
    private int buildNumber;
    private String status;
    private boolean building;
    private long duration;

    // Getters and Setters
    public int getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(int buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isBuilding() {
        return building;
    }

    public void setBuilding(boolean building) {
        this.building = building;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}