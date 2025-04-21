package com.techvvs.inventory.service.expense.constants;

public enum PaymentMethod {

    CASH,                 // Physical currency
    CHECK,                // Paper check
    ACH,                  // Automated Clearing House transfer
    WIRE_TRANSFER,        // Domestic or international wire
    CREDIT_CARD,          // Any corporate or personal credit card
    DEBIT_CARD,           // Less common for business, but possible
    MOBILE_PAYMENT,       // Venmo, Cash App, PayPal, etc.
    BANK_TRANSFER,        // Non-ACH direct transfers
    CRYPTOCURRENCY,       // BTC, ETH, etc. — if accepted
    STORE_CREDIT,         // Vendor-issued credits or rebates
    TRADE,                // Barter or product exchange
    OTHER                 // Catch-all / unknown / misc

}
