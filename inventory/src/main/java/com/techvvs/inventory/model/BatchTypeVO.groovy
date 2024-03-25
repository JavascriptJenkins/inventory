package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import javax.persistence.*
import java.time.LocalDateTime

@JsonIgnoreProperties
@Entity
@Table(name="batch_type")
class BatchTypeVO implements Serializable{


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Long batch_type_id;

    @JsonProperty
    String name

    @JsonProperty
    String description

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp



}
