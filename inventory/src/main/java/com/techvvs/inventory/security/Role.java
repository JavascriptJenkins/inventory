package com.techvvs.inventory.security;

import org.springframework.security.core.GrantedAuthority;

// These are the roles that are inside of the issued JWTs
public enum Role implements GrantedAuthority {
  ROLE_ADMIN, ROLE_CLIENT, ROLE_READ_ONLY, DELIVERY_DRIVER, EMPLOYEE, CHAT_USER, PUBLIC_QR_ROLE;

  public String getAuthority() {
    return name();
  }

}
