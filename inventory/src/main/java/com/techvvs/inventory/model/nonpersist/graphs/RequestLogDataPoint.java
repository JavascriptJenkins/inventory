package com.techvvs.inventory.model.nonpersist.graphs;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data point for request log analytics graphs
 */
public class RequestLogDataPoint {
    
    @JsonProperty
    private String label;
    
    @JsonProperty
    private Long count;
    
    @JsonProperty
    private Double avgDuration;
    
    public RequestLogDataPoint() {
    }
    
    public RequestLogDataPoint(String label, Long count, Double avgDuration) {
        this.label = label;
        this.count = count;
        this.avgDuration = avgDuration;
    }
    
    // Getters and Setters
    public String getLabel() {
        return label;
    }
    
    public void setLabel(String label) {
        this.label = label;
    }
    
    public Long getCount() {
        return count;
    }
    
    public void setCount(Long count) {
        this.count = count;
    }
    
    public Double getAvgDuration() {
        return avgDuration;
    }
    
    public void setAvgDuration(Double avgDuration) {
        this.avgDuration = avgDuration;
    }
    
    @Override
    public String toString() {
        return "RequestLogDataPoint{" +
                "label='" + label + '\'' +
                ", count=" + count +
                ", avgDuration=" + avgDuration +
                '}';
    }
}



