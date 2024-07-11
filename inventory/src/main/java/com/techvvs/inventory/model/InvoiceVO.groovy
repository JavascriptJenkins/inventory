package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.Table
import java.time.LocalDateTime


@JsonIgnoreProperties
@Entity
@Table(name="invoice")
class InvoiceVO implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer invoiceid // need to plz batchid to batchId in all usages

    @OneToMany
    @JoinColumn(name = "paymentid")
    List<PaymentVO> payment_list

    @OneToMany
    @JoinColumn(name = "batchid")
    List<ProductVO> product_list

    @ManyToOne
    @JoinColumn(name = "batchid")
    BatchVO batch;

    @JsonProperty
    @ManyToOne(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="customerid")
    CustomerVO customer;


    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;

}
