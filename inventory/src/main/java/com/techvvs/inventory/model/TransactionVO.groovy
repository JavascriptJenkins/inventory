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

    @JsonProperty
    @ElementCollection(fetch = FetchType.EAGER)
    Set<ProductVO> product_set

    @JsonProperty
    @ElementCollection(fetch = FetchType.EAGER)
    Set<PaymentVO> payment_set

    @JsonProperty
    @OneToOne(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="customerid")
    CustomerVO customervo;

    @JsonProperty
    Integer total;

    @JsonProperty
    Integer paid;

    @JsonProperty
    Integer taxes;

    @JsonProperty
    String notes

    @JsonProperty
    String cashier

    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp

}
