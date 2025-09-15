package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime

/* This is a configuration table to manually update the rewards system configuration */
@JsonIgnoreProperties
@Entity
@Table(name="rewardconfig")
class RewardConfigVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer rewardconfigid

    @JsonProperty
    int pointsperdollar;

    @JsonProperty
    Double pointvalue; // .01, .02, etc

    @JsonProperty
    int isactive;

    @JsonProperty
    String region; // filled by RewardRegion ENUM

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp

}
