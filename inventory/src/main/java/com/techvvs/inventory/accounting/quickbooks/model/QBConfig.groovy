package com.techvvs.inventory.accounting.quickbooks.model

import com.fasterxml.jackson.annotation.JsonProperty

import java.time.LocalDateTime;

// Store these per customer/company
class QBConfig {
    String realmId           // Company ID
    String accessToken       // OAuth token
    String refreshToken      // For token renewal

    @JsonProperty
    LocalDateTime tokenExpiry

    @JsonProperty
    LocalDateTime updateTimeStamp

    @JsonProperty
    LocalDateTime createTimeStamp
}