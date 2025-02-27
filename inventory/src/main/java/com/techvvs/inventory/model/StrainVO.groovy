package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime

// this entity is modeling lockers for automatic delivery pickup
@JsonIgnoreProperties
@Entity
@Table(name="strain")
class StrainVO implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer strainid;

    @JsonProperty
    String name;
    @JsonProperty
    String description;

    @JsonProperty
    String testingstatus;

    @JsonProperty
    Double thc;

    @JsonProperty
    Double cbd;

    @JsonProperty
    Double indica;

    @JsonProperty
    Double sativa;

    @JsonProperty
    String notes

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;


}
