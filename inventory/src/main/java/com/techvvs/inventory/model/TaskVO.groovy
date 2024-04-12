package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import javax.persistence.*
import java.time.LocalDateTime


@JsonIgnoreProperties
@Entity
@Table(name="task")
class TaskVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer task_id;

    @JsonProperty
    Integer tasknumber;

    @JsonProperty
    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    TaskTypeVO task_type_id

    @JsonProperty
    String name;

    @JsonProperty
    String notes;

    @JsonProperty
    String description




    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp

}
