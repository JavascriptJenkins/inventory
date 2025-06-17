package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.persistence.Transient
import java.time.LocalDateTime

@JsonIgnoreProperties
@Entity
@Table(name="attribute")
class AttributeVO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer attributeid

    @JsonProperty
    String name; // ie. size, color, material, brand (make this feed from ENUM of options for name value of attribute)

    @JsonProperty
    String value; // ie. XL, Blue, Wool, Bape

    @JsonProperty
    String description;

    @JsonProperty
    String iconUrl;

    @JsonProperty
    int active = 0 // this relates to how many products are being discounted in context of a Product Discount



    @ManyToMany
    @JoinTable(
            name = "product_attribute",
            joinColumns = @JoinColumn(name = "attributeid"),
            inverseJoinColumns = @JoinColumn(name = "productid")
    )
    List<ProductVO> product_attribute_list


    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;



}
