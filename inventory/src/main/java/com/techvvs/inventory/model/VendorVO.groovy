package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime


//@IdClass(BatchCompositeID.class)
@JsonIgnoreProperties
@Entity
@Table(name="vendor")
class VendorVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer vendorid

    @JsonProperty
    String name

    @JsonProperty
    String email

    @JsonProperty
    String address

    @JsonProperty
    String address2

    @JsonProperty
    String city;

    @JsonProperty
    String state;

    @JsonProperty
    String zipcode;

    @JsonProperty
    String phone

    @JsonProperty
    String website

    @JsonProperty
    String licensenumber

    @JsonProperty
    String notes

    @OneToMany(mappedBy = "vendor", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<ExpenseVO> expenses;

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp



}
