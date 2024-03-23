package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.persistence.*;
@JsonIgnoreProperties
@Entity
@Table(name="ProductType")
public class ProductTypeVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer product_type_id;
    @JsonProperty
    String name;
    @JsonProperty
    String description;
    // generic fields below
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    java.util.Date updateTimeStamp;

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    java.util.Date createTimeStamp;
                }
