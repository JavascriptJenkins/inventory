package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime


@IdClass(BatchCompositeID.class)
@JsonIgnoreProperties
@Entity
@Table(name="batch")
class BatchVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer batch_id

    @JsonProperty
    Integer batchnumber;

    @JsonProperty
    String name

    @JsonProperty
    String description

    @JsonProperty
    String notes

    @JsonProperty
    @ElementCollection(fetch = FetchType.EAGER)
    Set<BatchProductVO> batchproduct_set // list of batch products in this  batch.  these will be created new

    @JsonProperty
    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="batch_type_id")
    BatchTypeVO batch_type_id

    @JsonProperty
    @ElementCollection(fetch = FetchType.EAGER)
    Set<TaskVO> task_set

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp

}
