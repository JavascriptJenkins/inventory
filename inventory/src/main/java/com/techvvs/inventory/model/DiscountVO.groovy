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

    @JsonProperty
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="producttypeid")
    ProductTypeVO producttype

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
