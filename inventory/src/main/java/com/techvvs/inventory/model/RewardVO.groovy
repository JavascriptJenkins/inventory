package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime


//@IdClass(BatchCompositeID.class)
@JsonIgnoreProperties
@Entity
@Table(name="reward")
class RewardVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer rewardid

    @JsonProperty
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="transactionid")
    TransactionVO transaction;

    @JsonProperty
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="customerid")
    CustomerVO customer;

    @JsonProperty
    String actiontype // filled by RewardAction ENUM

    @JsonProperty
    int points;

    @JsonProperty
    String sourcechannel; // filled by RewardSourceChannel ENUM

    @JsonProperty
    String status; // filled by RewardStatus ENUM

    @JsonProperty
    String description // filled by RewardReason ENUM

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp

}
