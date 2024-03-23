package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*;
@JsonIgnoreProperties
@Entity
@Table(name="Product")
class ProductVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer product_id;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer product_type_id;
    @JsonProperty
    Integer quantity;
    @JsonProperty
    String name;
    @JsonProperty
    String description;
    @JsonProperty
    String barcode;
    @JsonProperty
    String price;
    @JsonProperty
    Integer sellPrice;
    @JsonProperty
    Integer salePrice;
    @JsonProperty
    Integer laborCostPricePerUnit;
    @JsonProperty
    Integer marginPercent;
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
    java.util.Date updateTimeStamp

    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty
    java.util.Date createTimeStamp
                }
