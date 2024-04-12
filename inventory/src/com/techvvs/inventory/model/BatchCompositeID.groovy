package com.techvvs.inventory.model

import javax.persistence.Entity;

public class BatchCompositeID implements Serializable {
    Integer batch_id;
    Integer batch_type_id;

    Integer getBatch_id() {
        return batch_id
    }

    void setBatch_id(Integer batch_id) {
        this.batch_id = batch_id
    }

    Integer getBatch_type_id() {
        return batch_type_id
    }

    void setBatch_type_id(Integer batch_type_id) {
        this.batch_type_id = batch_type_id
    }
// Getter and Setter
}
