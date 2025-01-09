package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime

// this entity is modeling lockers for automatic delivery pickup
@JsonIgnoreProperties
@Entity
@Table(name="locker")
class LockerVO implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer lockerid;
    @JsonProperty
    String name;
    @JsonProperty
    String description;
    @JsonProperty
    String lockerqrlink
    @JsonProperty
    String notes

    @JsonProperty
    @OneToOne(cascade= CascadeType.ALL, fetch  = FetchType.EAGER)
    @JoinColumn(name="locationid")
    LocationVO location

    @JsonProperty
    int isfull = 0

    @Transient
    PackageVO packageinscope

    @Transient
    int displayquantitytotalpackages

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;


}
