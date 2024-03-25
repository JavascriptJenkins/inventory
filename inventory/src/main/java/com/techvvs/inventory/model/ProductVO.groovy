package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.persistence.*;

@IdClass(ProductCompositeID.class)
@JsonIgnoreProperties
@Entity
@Table(name="Product")
public class ProductVO implements Serializable {

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
    // generic fields below
    @JsonProperty
    java.util.Date updateTimeStamp;

    @JsonProperty
    java.util.Date createTimeStamp;

    }
