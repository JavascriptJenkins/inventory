package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime


//@IdClass(BatchCompositeID.class)
@JsonIgnoreProperties
@Entity
@Table(name="payment")
class PaymentVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer paymentid

    @JsonProperty
    Integer paymentnumber;

    @JsonProperty
    Integer amountpaid;

    @JsonProperty
    String paymenttype;

    @JsonProperty
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="transactionid")
    TransactionVO transaction;

    @JsonProperty
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="customerid")
    CustomerVO customer;

    @JsonProperty
    String notes

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp

}
