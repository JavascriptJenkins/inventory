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

//    @JsonProperty
//    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
//    @JoinColumn(name="customerid")
//    CustomerVO customervo;

    // todo: make sure this is correct
    @JsonProperty
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="customerid")
    CustomerVO customervo;


    @JsonProperty
    String notes

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp

}
