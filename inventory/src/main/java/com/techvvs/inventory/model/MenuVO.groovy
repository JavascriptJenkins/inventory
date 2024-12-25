package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.CascadeType
import javax.persistence.ElementCollection
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
import javax.persistence.Transient
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

    @ManyToMany
    @JoinTable(
            name = "menu_product",
            joinColumns = @JoinColumn(name = "menuid"),
            inverseJoinColumns = @JoinColumn(name = "productid")
    )
    List<ProductVO> menu_product_list = new ArrayList<>()

    @JsonProperty
    @ElementCollection(fetch = FetchType.LAZY)
    List<DiscountVO> discount_list = new ArrayList<DiscountVO>()

    @JsonProperty
    Integer isdefault;

    @JsonProperty
    String notes

    @Transient
    String adhoc_label1 // for printing pages of adhoc labels

    @Transient
    String adhoc_label2 // for printing pages of adhoc labels

    @Transient
    String adhoc_label3 // for printing pages of adhoc labels

    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;

    /**
     * Apply discounts to the products in the menu_product_list.
     * @param discountVOs A list of DiscountVO objects to apply.
     */
    void applyDiscount(List<DiscountVO> discountVOs) {
        menu_product_list.each { product ->
            discountVOs.each { discount ->
                // Check for matching product type name and discount name
                if (product.producttypeid?.name == discount.name) {
                    // Apply the discount ensuring the display price does not go below 0
                    product.displayprice = Math.max(0, product.displayprice - discount.discountamount)
                }
            }
        }
    }



}
