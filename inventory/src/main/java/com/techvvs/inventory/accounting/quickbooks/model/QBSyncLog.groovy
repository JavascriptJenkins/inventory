package com.techvvs.inventory.accounting.quickbooks.model

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "qb_sync_log")
class QBSyncLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty
    Long id
    
    @Column(name = "entity_type", length = 50)
    String entityType     // "customer", "invoice", "payment"
    
    @Column(name = "entity_id")
    Integer entityId      // your internal ID
    
    @Column(name = "qb_id", length = 100)
    String qbId          // QuickBooks ID
    
    @Column(name = "status", length = 20)
    String status        // "pending", "synced", "error"
    
    @Column(name = "error_details", length = 1000)
    String errorDetails
    
    // generic fields below
    @JsonProperty
    @Column(name = "update_timestamp")
    LocalDateTime updateTimeStamp

    @JsonProperty
    @Column(name = "create_timestamp")
    LocalDateTime createTimeStamp
}