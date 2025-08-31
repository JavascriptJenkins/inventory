package com.techvvs.inventory.walletapi;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.*;
import java.util.Base64;

public final class GoogleJwtBuilder {
  private final String serviceAccountEmail;
  private final RSAPrivateKey privateKey;
  private final List<String> origins = new ArrayList<>();
  private final List<Map<String, Object>> loyaltyObjects = new ArrayList<>();

  private GoogleJwtBuilder(String serviceAccountEmail, RSAPrivateKey key) {
    this.serviceAccountEmail = serviceAccountEmail;
    this.privateKey = key;
  }

  /** Load from a Google service account JSON key (issuer you authorized in Google Wallet console). */
  public static GoogleJwtBuilder fromServiceAccount(Path jsonKeyPath) throws Exception {
    String json = Files.readString(jsonKeyPath);
    var map = new Gson().fromJson(json, Map.class);
    String clientEmail = (String) map.get("client_email");
    String privateKeyPem = (String) map.get("private_key");
    RSAPrivateKey key = parsePrivateKey(privateKeyPem);
    return new GoogleJwtBuilder(clientEmail, key);
  }

  public GoogleJwtBuilder addOrigin(String origin) {
    if (origin != null && !origin.isBlank()) origins.add(origin);
    return this;
  }

  /** Reference a pre-created LoyaltyObject by ID, e.g. "{issuerId}.{membershipNumber}". */
  public GoogleJwtBuilder loyaltyObjectReference(String objectId) {
    loyaltyObjects.add(Map.of("id", objectId));
    return this;
  }

  /** Embed a full LoyaltyObject JSON (keep JWT < ~1800 chars). */
  public GoogleJwtBuilder loyaltyObjectEmbedded(Map<String, Object> objectJson) {
    loyaltyObjects.add(objectJson);
    return this;
  }

  /** Builds the signed JWT and returns the Add to Google Wallet URL. */
  public String buildSaveUrl() {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("loyaltyObjects", loyaltyObjects);

    var now = Date.from(Instant.now());
    var alg = Algorithm.RSA256(null, privateKey);

    String jwt = JWT.create()
        .withIssuer(serviceAccountEmail)     // iss
        .withAudience("google")              // aud
        .withIssuedAt(now)                   // iat
        .withClaim("typ", "savetowallet")    // required claim
        .withArrayClaim("origins", origins.toArray(String[]::new))
        .withClaim("payload", payload)
        .sign(alg);

    return "https://pay.google.com/gp/v/save/" + jwt;
  }

  private static RSAPrivateKey parsePrivateKey(String pem) throws Exception {
    String clean = pem.replace("-----BEGIN PRIVATE KEY-----", "")
                      .replace("-----END PRIVATE KEY-----", "")
                      .replaceAll("\\s", "");
    byte[] der = Base64.getDecoder().decode(clean);
    var spec = new PKCS8EncodedKeySpec(der);
    return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
  }
}
