package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import java.time.LocalDateTime

@JsonIgnoreProperties
@Entity
@Table(name="billoflading")
class BillOfLadingVO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer billofladingid;
    @JsonProperty
    Integer billOfLadingNumber
    @JsonProperty
    String shipperName
    @JsonProperty
    String consigneeName
    @JsonProperty
    String carrierName
    @JsonProperty
    String origin
    @JsonProperty
    String destination

    @JsonProperty
    LocalDateTime shipmentDate
    @JsonProperty
    LocalDateTime estimatedDeliveryDate

    @JsonProperty
    @ElementCollection(fetch = FetchType.EAGER)
    List<CrateVO> crate_list

    @JsonProperty
    @ElementCollection(fetch = FetchType.LAZY)
    List<PackageVO> package_list

    @JsonProperty
    String termsAndConditions


    @JsonProperty
    LocalDateTime updateTimeStamp;
    @JsonProperty
    LocalDateTime createTimeStamp;


}
