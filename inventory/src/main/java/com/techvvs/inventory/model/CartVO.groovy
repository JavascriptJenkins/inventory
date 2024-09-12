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
import javax.persistence.OneToMany
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.persistence.Transient
import java.time.LocalDateTime


@JsonIgnoreProperties
@Entity
@Table(name="cart")
class CartVO implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer cartid

    @ManyToMany
    @JoinTable(
            name = "product_cart",
            joinColumns = @JoinColumn(name = "cartid"),
            inverseJoinColumns = @JoinColumn(name = "productid")
    )
    List<ProductVO> product_cart_list

    @OneToOne
    @JoinColumn(name = "customerid")
    CustomerVO customer

    @Transient
    String menuid

    @Transient
    String customerid

    // transient field for passing barcode on checkout page back to controller
    @Transient
    String barcode

    @Transient
    Integer displayquantitytotal // for displaying total units

    @JsonProperty
    Double total;

    @JsonProperty
    Integer isprocessed;

    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;


}
