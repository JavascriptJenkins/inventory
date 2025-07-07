package com.techvvs.inventory.model.metrc

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.techvvs.inventory.model.ExpenseVO
import com.techvvs.inventory.model.SystemUserDAO
import com.techvvs.inventory.model.VendorVO

import javax.persistence.*
import java.time.LocalDateTime

//@IdClass(BatchCompositeID.class)
@JsonIgnoreProperties
@Entity
@Table(name="metrclicense")
class MetrcLicenseVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer metrclicenseid

    @JsonProperty
    String licensenumber

    @JsonProperty
    String licensetype // filled by LicenseType ENUM

    @JsonProperty
    String notes

    @JsonProperty
    @ManyToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "id", referencedColumnName = "id")
    SystemUserDAO systemUserDAO;

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp



}
