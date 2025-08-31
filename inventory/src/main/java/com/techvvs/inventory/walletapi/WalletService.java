package com.techvvs.inventory.walletapi;

public interface WalletService {
  com.techvvs.inventory.walletapi.WalletIssueResult issueMembership(String membershipNumber, String memberName, String memberId) throws Exception;

  byte[] getPkpass(String membershipNumber) throws Exception;

  String getGoogleSaveUrl(String membershipNumber) throws Exception;

  record WalletIssueResult(
      String membershipNumber,
      String googleAddToWalletUrl
  ) {}
}
