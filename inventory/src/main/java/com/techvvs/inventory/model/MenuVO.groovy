package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.Table
import java.time.LocalDateTime

@JsonIgnoreProperties
@Entity
@Table(name="menu")
class MenuVO implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer menuid

    @JsonProperty
    String name

    // todo: probably don't need to eager fetch or cascade all here but whatever man
    @ManyToOne(cascade= CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "batchid")
    BatchVO batch;

    @ManyToMany
    @JoinTable(
            name = "menu_product",
            joinColumns = @JoinColumn(name = "menuid"),
            inverseJoinColumns = @JoinColumn(name = "productid")
    )
    List<ProductVO> menu_product_list

    @JsonProperty
    Integer isdefault;

    @JsonProperty
    String notes

    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;



}
