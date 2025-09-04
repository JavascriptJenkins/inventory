package com.techvvs.inventory.service.jenkins;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JenkinsJobResponse {
    private int number;
    private String result;
    private boolean building;
    private long duration;
    private String url;
    int getNumber() {
        return number
    }

    void setNumber(int number) {
        this.number = number
    }

    String getResult() {
        return result
    }

    void setResult(String result) {
        this.result = result
    }

    boolean getBuilding() {
        return building
    }

    void setBuilding(boolean building) {
        this.building = building
    }

    long getDuration() {
        return duration
    }

    void setDuration(long duration) {
        this.duration = duration
    }

    String getUrl() {
        return url
    }

    void setUrl(String url) {
        this.url = url
    }
}
