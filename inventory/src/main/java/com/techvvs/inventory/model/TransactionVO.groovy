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

    @JsonProperty
    Integer transactionnumber;

    @ManyToMany
    @JoinTable(
            name = "transaction_product",
            joinColumns = @JoinColumn(name = "transactionid"),
            inverseJoinColumns = @JoinColumn(name = "productid")
    )
    List<ProductVO> product_list

    @JsonProperty
    @ElementCollection(fetch = FetchType.EAGER)
    List<PaymentVO> payment_list

    @JsonProperty
    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="customerid")
    CustomerVO customervo;

    @JsonProperty
    Double total;

    @JsonProperty
    Double paid;

    @JsonProperty
    Integer taxpercentage;

    @JsonProperty
    String notes

    @JsonProperty
    String cashier

    // This will be 0 until the transaction is actually processed
    @JsonProperty
    Integer isprocessed

    @JsonProperty
    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="cartid")
    CartVO cart // only gets saved here after the user submits the transaction

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp


    PaymentVO getMostRecentPayment() {
        if (payment_list == null || payment_list.isEmpty()) {
            return new PaymentVO(paymentid: 0)
        }
        payment_list.max { it.createTimeStamp }
    }

}
