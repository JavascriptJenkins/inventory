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
    Double discountamount;

    @JsonProperty
    Double discountpercentage;

    @JsonProperty
    String name;

    @JsonProperty
    String description;

    @JsonProperty
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="transactionid")
    TransactionVO transaction;

    @JsonProperty
    String notes

    @Transient
    Double discountdisplayamount;

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp

}
