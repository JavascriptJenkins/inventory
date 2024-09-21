package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime

@JsonIgnoreProperties
@Entity
@Table(name="pallet")
class PalletVO implements Serializable{

    // Pallets and Packages both can be tied to a seperate delivery/transaction/customer

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer palletid;

    @JsonProperty
    String name;

    @JsonProperty
    String description;

    @JsonProperty
    String palletbarcode;

    @OneToOne
    @JoinColumn(name = "customerid")
    CustomerVO customer

    @JsonProperty
    @ElementCollection(fetch = FetchType.EAGER)
    List<CrateVO> crate_list

    @JsonProperty
    @ManyToOne(cascade=CascadeType.REFRESH, fetch = FetchType.EAGER)
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

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;

    @Transient
    int quantityselected = 0


}
