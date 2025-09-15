package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime

// This maps to concept of "growth phase" in metrc and will also be used for other purposes too
@JsonIgnoreProperties
@Entity
@Table(name="planttype")
class PlantTypeVO implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer planttypeid;
    @JsonProperty
    String name; // IMMATURE, VEGGING, FLOWERING
    @JsonProperty
    String description;

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;


}
