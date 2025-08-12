package com.techvvs.inventory.service.metrc.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LocationTypeVO {

    @JsonProperty("Id")
    private Long id;

    @JsonProperty("Name")
    private String name;

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
