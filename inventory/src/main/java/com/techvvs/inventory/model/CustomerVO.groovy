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
    Integer customernumber;

    @JsonProperty
    String name

    @JsonProperty
    String email

    @JsonProperty
    String address

    @JsonProperty
    String address2

    @JsonProperty
    String phone

    @JsonProperty
    String notes

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp

}
