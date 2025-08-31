package com.techvvs.inventory.walletapi;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "wallet_artifacts")
public class WalletArtifact {

  @Id
  @Column(name = "membership_number", nullable = false, updatable = false)
  private String membershipNumber;

  // Store binary as bytea in Postgres
  @Lob
  @Column(name = "apple_pkpass", columnDefinition = "bytea")
  private byte[] applePkpass;

  @Column(name = "google_save_url", length = 2000)
  private String googleSaveUrl;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected WalletArtifact() {}

  public WalletArtifact(String membershipNumber, byte[] applePkpass, String googleSaveUrl) {
    this.membershipNumber = membershipNumber;
    this.applePkpass = applePkpass;
    this.googleSaveUrl = googleSaveUrl;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  @PreUpdate
  public void touch() { this.updatedAt = Instant.now(); }

  // getters/setters
  public String getMembershipNumber() { return membershipNumber; }
  public byte[] getApplePkpass() { return applePkpass; }
  public void setApplePkpass(byte[] b) { this.applePkpass = b; touch(); }
  public String getGoogleSaveUrl() { return googleSaveUrl; }
  public void setGoogleSaveUrl(String s) { this.googleSaveUrl = s; touch(); }
  public Instant getCreatedAt() { return createdAt; }
  public Instant getUpdatedAt() { return updatedAt; }
}
