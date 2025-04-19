package com.techvvs.inventory.service.rewards.constants;

public enum RewardReason {

    // Bonus & Promotions
    BONUS,                // Generic promotional bonus
    SIGNUP_BONUS,         // Points for account creation
    BIRTHDAY_BONUS,       // Birthday reward
    PROMO_CODE,           // Entered promo/referral code
    REVIEW_BONUS,         // Left a product review
    SOCIAL_SHARE,         // Shared on social media

    // Referral / Social
    REFERRAL_EARN,        // Referrer earned points
    REFERRAL_REDEEM,      // Referral resulted in redemption
    FRIEND_SIGNUP,        // Friend joined, reward earned

    // Order Related
    ORDER_CANCELLED,      // Order cancelled and points removed
    ORDER_RETURNED,       // Returned item, points reversed
    PARTIAL_REFUND,       // Partial reversal of points

    // Admin / System
    ADMIN_ADJUSTMENT,     // Manually adjusted by admin
    SYSTEM_MIGRATION,     // Legacy or import data
    ROLLBACK,             // Auto reversal or fraud mitigation

    // Optional / Gamification
    LEVEL_UP,             // Loyalty tier reached
    ANNIVERSARY,          // Account anniversary reward
    GAMIFICATION_REWARD,  // Contest or challenge reward
    MILESTONE             // Hit milestone like "10 orders"

}
