package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.persistence.*
import java.time.LocalDateTime;

//@IdClass(ProductCompositeID.class)
@JsonIgnoreProperties
@Entity
@Table(name="Product")
public class ProductVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer product_id;
    @JsonProperty
    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="producttypeid")
    ProductTypeVO producttypeid;
    @JsonProperty
    Integer productnumber;
    @JsonProperty
    Integer quantity;
    @JsonProperty
    String notes
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
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;

    }
