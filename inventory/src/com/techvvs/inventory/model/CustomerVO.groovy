package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime


//@IdClass(BatchCompositeID.class)
@JsonIgnoreProperties
@Entity
@Table(name="customer")
class CustomerVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer customerid

    @JsonProperty
    String membershipnumber

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
    String notes

    @JsonProperty
    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="deliveryid")
    DeliveryVO delivery

    @JsonProperty
    @Column(name = "shoppingtoken", length = 1024)
    String shoppingtoken // This stores a customer's currently active shoppingtoken

    @Transient
    Integer shoppingtokenexpired

    @JsonProperty
    @ElementCollection(fetch = FetchType.LAZY)
    List<LocationVO> locationlist = new ArrayList<LocationVO>()

    // These fields are for QuickBooks sync
    @JsonProperty
    String quickbooksId

    @JsonProperty
    LocalDateTime lastQBSync

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp

    @JsonProperty
    Integer deleted = 0


    String getEncodedShoppingtoken() {
        if (shoppingtoken == null) return null
        Base64.getUrlEncoder().withoutPadding().encodeToString(shoppingtoken.getBytes(StandardCharsets.UTF_8));
    }

}
