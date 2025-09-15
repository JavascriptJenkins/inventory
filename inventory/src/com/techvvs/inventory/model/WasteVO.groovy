package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime


@JsonIgnoreProperties
@Entity
@Table(name="waste")
class WasteVO implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer wasteid;

    @JsonProperty
    String name;
    @JsonProperty
    String description;

    @JsonProperty
    String notes


    @JsonProperty
    String method;

    @JsonProperty
    String reason;

    @JsonProperty
    Double weight;

    @JsonProperty
    String materialmixed;


    @JsonProperty
    LocalDateTime wastedate;


    @JsonProperty
    @OneToOne(cascade= CascadeType.ALL, fetch  = FetchType.LAZY)
    @JoinColumn(name="locationid")
    LocationVO location


    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;


}
