package com.techvvs.inventory.service.paypal;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaypalTokenResponse {
    @JsonProperty("access_token")
    public String accessToken;

    @JsonProperty("token_type")
    public String tokenType;

    @JsonProperty("expires_in")
    public Long expiresIn;  // seconds

    public String scope;
}
