package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime


//@IdClass(BatchCompositeID.class)
@JsonIgnoreProperties
@Entity
@Table(name="expense")
class ExpenseVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer expenseid

    @JsonProperty
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="batchid")
    BatchVO batch;

    @JsonProperty
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "vendorid", referencedColumnName = "vendorid")
    VendorVO vendor;

    @JsonProperty
    Double amount

    @JsonProperty
    String paymentmethod // filled by PaymentMethod ENUM

    @JsonProperty
    String expensetype // filled by ExpenseType ENUM

    @JsonProperty
    String notes

    @JsonProperty
    int systemuser

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp

}
