package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.CascadeType
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.Table
import java.time.LocalDateTime

@JsonIgnoreProperties
@Entity
@Table(name="Package")
class PackageVO implements Serializable{

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
    Integer packageid;
    @JsonProperty
    String name;
    @JsonProperty
    String description;

    @JsonProperty
    @OneToOne(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="packagetypeid")
    ProductTypeVO packagetype;

    @ManyToMany
    @JoinTable(
            name = "package_product",
            joinColumns = @JoinColumn(name = "packageid"),
            inverseJoinColumns = @JoinColumn(name = "productid")
    )
    List<ProductVO> product_package_list


    @JsonProperty
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="deliveryid")
    DeliveryVO delivery

    @JsonProperty
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name="transactionid")
    TransactionVO transaction


    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;

}
