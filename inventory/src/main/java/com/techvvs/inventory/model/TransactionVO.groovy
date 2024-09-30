package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

import javax.persistence.*
import java.time.LocalDateTime


//@IdClass(BatchCompositeID.class)
@JsonIgnoreProperties
@Entity
@Table(name="transaction")
class TransactionVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer transactionid // need to plz batchid to batchId in all usages

    @ManyToMany
    @JoinTable(
            name = "transaction_product",
            joinColumns = @JoinColumn(name = "transactionid"),
            inverseJoinColumns = @JoinColumn(name = "productid")
    )
    List<ProductVO> product_list

    @JsonProperty
    @ElementCollection(fetch = FetchType.LAZY)
    List<PaymentVO> payment_list

    @JsonProperty
    @ElementCollection(fetch = FetchType.LAZY)
    List<ReturnVO> return_list

    @JsonProperty
    @ElementCollection(fetch = FetchType.EAGER)
    List<PackageVO> package_list // this package list would be packages getting sold without being associated to a delivery

    @JsonProperty
    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="customerid")
    CustomerVO customervo;

    @JsonProperty
    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="deliveryid")
    DeliveryVO delivery; // delivery contains the list of packages and crates

    @JsonProperty
    Double total;

    @JsonProperty
    Double totalwithtax;

    @JsonProperty
    Double paid;

    @JsonProperty
    Double taxpercentage;

    @JsonProperty
    String notes

    @JsonProperty
    String cashier

    // This will be 0 until the transaction is actually processed
    @JsonProperty
    Integer isprocessed

    // if this is 1, we know to display the package_set and we know we are dealing with a package transaction
    @JsonProperty
    Integer ispackagetype

    @JsonProperty
    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="cartid")
    CartVO cart // only gets saved here after the user submits the transaction

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp

    @Transient
    Integer displayquantitytotal // for displaying total units

    @Transient
    String action = ""

    @Transient
    String phonenumber = ""

    @Transient
    String email = ""

    @Transient
    String filename = ""


    PaymentVO getMostRecentPayment() {
        if (payment_list == null || payment_list.isEmpty()) {
            return new PaymentVO(paymentid: 0)
        }
        payment_list.max { it.createTimeStamp }
    }

}
