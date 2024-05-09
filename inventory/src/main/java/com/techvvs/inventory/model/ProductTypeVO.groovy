package com.techvvs.inventory.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.persistence.*
import java.time.LocalDateTime;

@JsonIgnoreProperties
@Entity
@Table(name="ProductType")
class ProductTypeVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer producttypeid;
    @JsonProperty
    String name;
    @JsonProperty
    String description;
    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;

    @Transient
    Integer pagesize

}

