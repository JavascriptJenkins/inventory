package com.techvvs.inventory.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import javax.persistence.*
import java.time.LocalDateTime

//@IdClass(BatchProductCompositeID.class)
@JsonIgnoreProperties
@Entity
@Table(name="batchproduct")
class BatchProductVO implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    Integer batchproduct_id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="product_id")
    @JoinColumn(name="product_type_id")
    ProductVO product_id;

    @JsonProperty
    String notes;

    @JsonProperty
    Integer status_type; // can be integers just mapped to values somewhere for now

    @JsonProperty
    Long upcabarcode; // 12 digit unique barcode


    // generic fields below
    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp
}
