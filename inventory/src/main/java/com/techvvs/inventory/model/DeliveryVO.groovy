package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime

@JsonIgnoreProperties
@Entity
@Table(name="Delivery")
class DeliveryVO implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer deliveryid;
    @JsonProperty
    String name;
    @JsonProperty
    String description;

    @JsonProperty
    @OneToOne(cascade= CascadeType.ALL, fetch  = FetchType.EAGER)
    @JoinColumn(name="locationid")
    LocationVO destination

    @JsonProperty
    @ElementCollection(fetch = FetchType.EAGER)
    List<PackageVO> package_list

    @JsonProperty
    int iscanceled;

    @JsonProperty
    int isdelivered;

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;


}
