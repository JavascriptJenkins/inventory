package com.techvvs.inventory.model;

public class BatchProductCompositeID implements Serializable {
    Integer batchproduct_id;

    Integer getBatchproduct_id() {
        return batchproduct_id
    }

    void setBatchproduct_id(Integer batchproduct_id) {
        this.batchproduct_id = batchproduct_id
    }

    Integer getProduct_id() {
        return product_id
    }

    void setProduct_id(Integer product_id) {
        this.product_id = product_id
    }
    Integer product_id;

    // Getter and Setter
}
