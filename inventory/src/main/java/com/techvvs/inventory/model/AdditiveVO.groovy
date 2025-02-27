package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime


@JsonIgnoreProperties
@Entity
@Table(name="additive")
class AdditiveVO implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer additiveid;

    @JsonProperty
    String name;

    @JsonProperty
    String description;

    @JsonProperty
    String eparegnumber;

    @JsonProperty
    String supplier;
    @JsonProperty
    String applicationdevice;

    @JsonProperty
    Double amount;
    @JsonProperty
    String amountdesc;

    @JsonProperty
    Double percentage;

    @JsonProperty
    @ElementCollection(fetch = FetchType.LAZY)
    Set<IngredientVO> ingredient_set = new HashSet<>() // maps to concept of "activeingredient" in metrc

    @JsonProperty
    LocalDateTime dateapplied;

    @JsonProperty
    String notes

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;


}
