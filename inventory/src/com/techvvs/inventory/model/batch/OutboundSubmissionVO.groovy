package com.techvvs.inventory.model.batch

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.techvvs.inventory.model.SystemUserDAO

import javax.persistence.*
import java.time.LocalDateTime

//@IdClass(BatchCompositeID.class)
@JsonIgnoreProperties
@Entity
@Table(name="outboundsubmission")
class OutboundSubmissionVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer outboundsubmissionid

    @JsonProperty
    String payload

    @JsonProperty
    int submitted = 0

    @JsonProperty
    int attempts = 0

    @JsonProperty
    String uri // what uri are we submitting too

    @JsonProperty
    String httpmethod // POST, UPDATE, etc.

    @JsonProperty
    String requestbody // physical requestbody when the request is sent

    @JsonProperty
    String requestheaders // physical requestheaders when the request is sent

    @JsonProperty
    String responsebody // physical responsebody when the request is sent

    @JsonProperty
    String responseheaders // physical responseheaders when the request is sent

    @JsonProperty
    String statuscode // 200,400,etc

    @JsonProperty
    String type // RETAIL_TRANSACTION, WHOLESALE_TRANSFER, etc. Make an Enum for this with all the METRC types

    @Transient
    Object outgoingDTO

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp



}
