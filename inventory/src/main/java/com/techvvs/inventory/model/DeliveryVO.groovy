package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime

@JsonIgnoreProperties
@Entity
@Table(name="delivery")
class DeliveryVO implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer deliveryid;
    @JsonProperty
    String name;
    @JsonProperty
    String description;
    @JsonProperty
    String deliverybarcode

    @JsonProperty
    @OneToOne(cascade= CascadeType.ALL, fetch  = FetchType.EAGER)
    @JoinColumn(name="locationid")
    LocationVO destination

    @JsonProperty
    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="transactionid")
    TransactionVO transaction;

    // NOTE: A delivery can either have a list of packages, or a list of pallets
    @JsonProperty
    @ElementCollection(fetch = FetchType.LAZY)
    List<PackageVO> package_list

    @JsonProperty
    @ElementCollection(fetch = FetchType.LAZY)
    List<CrateVO> crate_list

    @JsonProperty
    int iscanceled;

    @JsonProperty
    int isprocessed;

    @Transient
    PackageVO packageinscope

    @Transient
    int displayquantitytotal

    // transient field for passing barcode on checkout page back to controller
    @Transient
    String barcode

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;


}
