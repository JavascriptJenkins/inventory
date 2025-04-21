package com.techvvs.inventory.service.expense.constants;

public enum ExpenseType {

    // Core production costs
    RAW_MATERIALS,         // Biomass, distillate, rosin, terpenes
    PACKAGING,             // Jars, pouches, boxes, labels
    LAB_TESTING,           // Compliance or R&D testing
    PROCESSING,            // Co-packing, extraction, trimming

    // Operational costs
    LABOR,                 // Wages or contracted labor
    UTILITIES,             // Electricity, water, gas
    EQUIPMENT,             // Equipment purchases or rentals
    MAINTENANCE,           // Repairs, cleaning, calibration

    // Business admin
    LICENSING_FEES,        // State fees, renewal costs
    INSURANCE,             // General liability, crop coverage
    RENT,                  // Facility leases
    LEGAL_FEES,            // Attorneys, regulatory help
    ACCOUNTING_FEES,       // Bookkeeping, CPA services

    // Sales & Distribution
    MARKETING,             // Promo materials, paid ads
    DELIVERY,              // Fuel, third-party logistics
    DISTRIBUTION_FEES,     // Wholesale commissions, fulfillment

    // Tech & Systems
    SOFTWARE,              // SaaS tools, Metrc integrations
    HARDWARE,              // Printers, scales, POS devices

    // Other
    TRAINING,              // Staff education, certifications
    SECURITY,              // Cameras, security staff, alarm systems
    TRAVEL,                // Conferences, vendor visits
    MISC                  // Anything uncategorized



}
