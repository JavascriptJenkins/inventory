package com.techvvs.inventory.model.metrc

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.techvvs.inventory.model.SystemUserDAO

import javax.persistence.*
import java.time.LocalDateTime

//@IdClass(BatchCompositeID.class)
@JsonIgnoreProperties
@Entity
@Table(name="transfer")
class TransferVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer transferid

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
