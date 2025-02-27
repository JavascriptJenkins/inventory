package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table
import java.time.LocalDateTime

@JsonIgnoreProperties
@Entity
@Table(name="uccbarcode")
class UccBarcode {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer uccbarcodeid;

    // 106141412345678905
    @JsonProperty
    String sscc18;

    // 9876543210
    @JsonProperty
    String ponumber;

    // 1234567890123
    @JsonProperty
    String shiptogln;

    // 01234567890128
    @JsonProperty
    String gtin;

    // ABC123
    @JsonProperty
    String lotnumber;

    // 2024-12-31
    @JsonProperty
    LocalDateTime expirydate;

    // (00)106141412345678905(400)9876543210(410)1234567890123(01)01234567890128(10)ABC123(17)241231
    @JsonProperty
    String fullencodedstring;


    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;


    // details on ucc-128 barcodes
//    A single barcode can include:
//
//    Serial Shipping Container Code (SSCC) → AI (00)
//    Purchase Order Number → AI (400)
//    Ship-To Address (GLN - Global Location Number) → AI (410)
//    Product GTIN (Global Trade Item Number) → AI (01)
//    Lot Number → AI (10)
//    Expiration Date → AI (17)

    // (00)106141412345678905(400)9876543210(410)1234567890123(01)01234567890128(10)ABC123(17)241231
//
//    SSCC-18	(00)	106141412345678905
//    Purchase Order Number	(400)	9876543210
//    Ship-To Address (GLN)	(410)	1234567890123
//    Product GTIN	(01)	01234567890128
//    Lot Number	(10)	ABC123
//    Expiration Date	(17)	241231 (Dec 31, 2024)


}
