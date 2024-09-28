package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table
import java.time.LocalDateTime

@JsonIgnoreProperties
@Entity
@Table(name="location")
class LocationVO implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer locationid;
    @JsonProperty
    String name;
    @JsonProperty
    String description;
    @JsonProperty
    @OneToOne(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="locationtypeid")
    LocationTypeVO locationtype;

    @JsonProperty
    String address1;
    @JsonProperty
    String address2;
    @JsonProperty
    String city;
    @JsonProperty
    String state;
    @JsonProperty
    String zipcode;

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;


}
