package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.Nullable
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
import javax.persistence.OneToOne
import javax.persistence.Table
import javax.persistence.Transient
import java.time.LocalDateTime

@JsonIgnoreProperties
@Entity
@Table(name="package")
class PackageVO implements Serializable{

    // Pallets and Packages both can be tied to a seperate delivery/transaction/customer

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer packageid;
    @JsonProperty
    String name;
    @JsonProperty
    String description;
    @JsonProperty
    String packagebarcode;

    // This is a many to one relationship because
    // we could have multiple packages with the same uccbarcode (multiple packages under a single METRC transfer)
    @JsonProperty
    @ManyToOne(cascade=CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinColumn(name="uccbarcodeid")
    UccBarcode uccBarcode

    @JsonProperty
    @OneToOne(cascade= CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name="packagetypeid")
    PackageTypeVO packagetype;

    @OneToOne
    @JoinColumn(name = "customerid")
    CustomerVO customer

    @JsonProperty
    @ManyToOne(cascade=CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name="deliveryid")
    DeliveryVO delivery

    @ManyToMany
    @JoinTable(
            name = "package_product",
            joinColumns = @JoinColumn(name = "packageid"),
            inverseJoinColumns = @JoinColumn(name = "productid")
    )
    List<ProductVO> product_package_list

    @JsonProperty
    @ManyToOne(cascade=CascadeType.REFRESH, fetch = FetchType.EAGER)
    @JoinColumn(name="crateid")
    CrateVO crate

    @JsonProperty
    @ManyToOne(cascade=CascadeType.REFRESH, fetch = FetchType.LAZY)
    @JoinColumn(name="transactionid")
    TransactionVO transaction

    @JsonProperty
    Double total;

    @JsonProperty
    int isprocessed

    @JsonProperty
    Double weight

    @JsonProperty
    String bagcolor

    @Transient
    Integer displayquantitytotal // for displaying total units

    @Transient
    String barcode;

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp;

    @JsonProperty
    LocalDateTime createTimeStamp;

    @Transient
    int quantityselected = 0


}
