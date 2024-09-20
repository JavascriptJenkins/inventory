package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime

@JsonIgnoreProperties
@Entity
@Table(name="Pallet")
class PalletVO implements Serializable{

    // Steps:
// navigate to ui to create a package
// add your products to the package by scanning the barcodes (when first barcode is scanned it will create the package in db)
// hit "package review" button and navigate to the package review ui
// when "submit" button clicked on package review ui, create a packagetype transaction and create "transaction barcodes" and invoice

    // general notes
// create the concept of "product checkout" and "package checkout"
// navigate to "package checkout" screen
// add the packages to the cart by scanning their barcodes
// duplicate the cart review page but for packages
// when we checkout the cart full of packages, create a transaction in the database

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer palletid;

    @JsonProperty
    String name;

    @JsonProperty
    String description;

    @JsonProperty
    String barcode;

    @OneToOne
    @JoinColumn(name = "customerid")
    CustomerVO customer

    @JsonProperty
    @ElementCollection(fetch = FetchType.EAGER)
    List<PackageVO> package_list

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
