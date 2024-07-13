package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import javax.persistence.*
import java.time.LocalDateTime;

//@IdClass(ProductCompositeID.class)
@JsonIgnoreProperties
@Entity
@Table(name="product")
public class ProductVO implements Serializable {
// todo: make a productsale object
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer product_id;
    @ManyToOne
    @JoinColumn(name = "batchid")
    BatchVO batch;
    @JsonProperty
    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="producttypeid")
    ProductTypeVO producttypeid;
    @JsonProperty
    @ManyToMany(mappedBy = "product_cart_list")
    List<CartVO> cart_list
    @JsonProperty
    @ManyToMany(mappedBy = "product_list")
    List<TransactionVO> transaction_list
    @JsonProperty
    Integer productnumber;
    @JsonProperty
    Integer quantity;
    @JsonProperty
    Integer quantityremaining;
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
    Integer cost;
    @JsonProperty
    Integer salePrice;
    @JsonProperty
    Integer laborCostPricePerUnit;
    @JsonProperty
    Integer marginPercent;

    // for display purposes only
    @Transient
    Integer displayquantity

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;

    }
