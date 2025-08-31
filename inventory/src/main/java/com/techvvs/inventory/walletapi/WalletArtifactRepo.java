package com.techvvs.inventory.walletapi;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletArtifactRepo extends JpaRepository<WalletArtifact, String> {
  // id = membershipNumber
}
