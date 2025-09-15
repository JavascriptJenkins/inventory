package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime


//@IdClass(BatchCompositeID.class)
@JsonIgnoreProperties
@Entity
@Table(name="discount")
class DiscountVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer discountid

    @JsonProperty
    Double discountamount = 0

    // todo: delete this from app0.0
    @JsonProperty
    Double discountpercentage = 0

    @JsonProperty
    String name;

    @JsonProperty
    String description;

    @JsonProperty
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="transactionid")
    TransactionVO transaction

    @JsonProperty
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="menuid")
    MenuVO menu

    // We are letting the users discount things by product type, or by product
    @JsonProperty
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="producttypeid")
    ProductTypeVO producttype


    @JsonProperty
    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="productid")
    ProductVO product
    @JsonProperty
    int quantity = 0 // this relates to how many products are being discounted in context of a Product Discount

    @JsonProperty
    String notes

    @JsonProperty
    int isactive // only 1 active discount per producttype

    @Transient
    Double discountdisplayamount;

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp

}
