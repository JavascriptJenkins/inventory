package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime

@JsonIgnoreProperties
@Entity
@Table(name="crate")
class CrateVO implements Serializable{

// NOTE: crate is meant to be a thin abstraction.  We are handling the Transaction/Delivery/Customer relationships
    // on the Packages/Pallets
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer crateid;

    @JsonProperty
    String name;

    @JsonProperty
    String description;

    @JsonProperty
    String cratebarcode;

    @JsonProperty
    @ElementCollection(fetch = FetchType.EAGER)
    List<PackageVO> package_list

    @OneToOne
    @JoinColumn(name = "customerid")
    CustomerVO customer

    @JsonProperty
    @ManyToOne(cascade=CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinColumn(name="deliveryid")
    DeliveryVO delivery

    @JsonProperty
    @ManyToOne(cascade=CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinColumn(name="transactionid")
    TransactionVO transaction

    @JsonProperty
    Double total;

    @JsonProperty
    int isprocessed

    @JsonProperty
    Double weight

    @Transient
    int displayquantitytotal









    @Transient
    PackageVO packageinscope

    @Transient
    String barcode

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;



}
