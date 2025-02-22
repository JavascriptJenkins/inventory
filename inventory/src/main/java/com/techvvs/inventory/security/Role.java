package com.techvvs.inventory.security;

import org.springframework.security.core.GrantedAuthority;

// These are the roles that are inside of the issued JWTs
public enum Role implements GrantedAuthority {
  ROLE_ADMIN, ROLE_SUPER_ADMIN, ROLE_CLIENT, ROLE_READ_ONLY, DELIVERY_DRIVER, EMPLOYEE, CHAT_USER, PUBLIC_QR_ROLE, ROLE_DOWNLOAD_LINK, ROLE_SHOPPING_TOKEN, ROLE_DELIVERY_VIEW_TOKEN, ROLE_DELIVERY_EMPLOYEE_VIEW_TOKEN, ROLE_MEDIA_ONLY;

  public String getAuthority() {
    return name();
  }

}
