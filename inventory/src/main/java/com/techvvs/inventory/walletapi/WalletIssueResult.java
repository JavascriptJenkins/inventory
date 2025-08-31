package com.techvvs.inventory.walletapi;

public record WalletIssueResult(
    byte[] applePkpass,                // Content-Type: application/vnd.apple.pkpass
    String googleAddToWalletUrl        // “Add to Google Wallet” deep link
) {}