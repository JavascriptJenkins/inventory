package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime


//@IdClass(BatchCompositeID.class)
@JsonIgnoreProperties
@Entity
@Table(name="tenant")
class TenantVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer tenantid

    @JsonProperty
    String name // used for display purposes

    @JsonProperty
    String subdomain // could be different than "name"

    @JsonProperty
    String key // this will be a UUID generated for dbname

    @JsonProperty
    String status // controlled by a TenantStatus enum

    @JsonProperty
    String billingplan // controlled by a TenantBillingPlan enum

    @JsonProperty
    String email // whoever created this tenant when signing up

    @JsonProperty
    String phone // whoever created this tenant when signing up

    @JsonProperty
    String env

    @JsonProperty
    String appuri

    @JsonProperty
    String legalname // LLC name etc

    @JsonProperty
    String licensenumber // business specific license number if licensed with the state somewhere

    @JsonProperty
    String industry // controlled by enum TenantIndustry


    @JsonProperty
    @ElementCollection(fetch = FetchType.LAZY)
    List<SystemUserDAO> systemuserlist = new ArrayList<SystemUserDAO>()


    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp

}
