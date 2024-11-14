package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
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
    DeliveryVO delivery;

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp

}
