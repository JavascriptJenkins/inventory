package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime


@JsonIgnoreProperties
@Entity
@Table(name="plant")
class PlantVO implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer plantid;

    @JsonProperty
    @OneToOne(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="planttypeid")
    PlantTypeVO planttype; // this tracks growth phases, isdestroyed, waste, etc

    @JsonProperty
    @OneToOne(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="strainid")
    StrainVO strain;

    @JsonProperty
    String name;
    @JsonProperty
    String description;
    @JsonProperty
    int isdestroyed = 0
    @JsonProperty
    String notes

    @JsonProperty
    @OneToOne(cascade= CascadeType.ALL, fetch  = FetchType.LAZY)
    @JoinColumn(name="locationid")
    LocationVO location


    @JsonProperty
    @ElementCollection(fetch = FetchType.LAZY)
    Set<AdditiveVO> additive_set = new HashSet<>() // maps to concept of "additives" in metrc
    @JsonProperty
    @ElementCollection(fetch = FetchType.LAZY)
    Set<WasteVO> waste_set = new HashSet<>() // maps to concept of "plant waste" in metrc



    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;


}
