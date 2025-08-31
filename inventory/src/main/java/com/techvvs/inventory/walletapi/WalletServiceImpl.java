package com.techvvs.inventory.walletapi;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.gson.Gson;
import de.brendamour.jpasskit.*;
import de.brendamour.jpasskit.enums.PKBarcodeFormat;
import de.brendamour.jpasskit.enums.PKPassType;
import de.brendamour.jpasskit.passes.PKGenericPass;
import de.brendamour.jpasskit.signing.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;

@Service
@Transactional
public class WalletServiceImpl implements WalletService {

  private final WalletArtifactRepo repo;

  public WalletServiceImpl(WalletArtifactRepo repo) {
    this.repo = repo;
  }

  // ---------- Apple Wallet config ----------
//  @Value("${wallet.apple.passCertPath}")        private String passCertPath;   // .p12
//  @Value("${wallet.apple.passCertPassword}")    private String passCertPwd;
//  @Value("${wallet.apple.appleWWDRCertPath}")   private String wwdrPath;       // Apple WWDR
//  @Value("${wallet.apple.passTypeIdentifier}")  private String passTypeId;
//  @Value("${wallet.apple.teamIdentifier}")      private String teamId;
//  @Value("${wallet.apple.templateFolder}")      private String templateFolder;  // contains icon.png, logo.png, etc.
//  @Value("${wallet.apple.webServiceUrl:}")      private String webServiceUrl;   // optional
//  @Value("${wallet.apple.authToken:}")          private String authToken;       // optional

  // ---------- Google Wallet config ----------
  @Value("${wallet.google.serviceAccountKeyPath}") private String gsaKeyPath;   // JSON key file
  @Value("${wallet.google.issuerId}")              private String issuerId;     // e.g., 3388000000000000000
  @Value("${wallet.google.classId}")               private String loyaltyClassId;// e.g., issuerId.programId
  @Value("${wallet.brand.organizationName:Your Brand}") private String orgName;
  @Value("${wallet.brand.programName:Membership}")       private String programName;

  @Override
  public com.techvvs.inventory.walletapi.WalletIssueResult issueMembership(String membershipNumber, String memberName, String memberId) throws Exception {
    // If already issued, return existing to keep POST idempotent-ish (or flip to PUT for full idempotency)
    WalletArtifact existing = repo.findById(membershipNumber).orElse(null);

    // Build Google Save URL (JWT with embedded object)
    String googleUrl = buildGoogleSaveUrlEmbedded(membershipNumber, memberName);

    // Build Apple .pkpass
//    byte[] pkpass = buildApplePkpass(membershipNumber, memberName);

//    WalletArtifact art = (existing == null)
//            ? new WalletArtifact(membershipNumber, pkpass, googleUrl)
//            : updateExisting(existing, pkpass, googleUrl);

//    repo.save(art);

    return new com.techvvs.inventory.walletapi.WalletIssueResult(membershipNumber.getBytes(), googleUrl);
  }

  private WalletArtifact updateExisting(WalletArtifact a, byte[] pkpass, String url) {
    a.setApplePkpass(pkpass);
    a.setGoogleSaveUrl(url);
    return a;
  }

  @Override
  @Transactional(readOnly = true)
  public byte[] getPkpass(String membershipNumber) {
    return repo.findById(membershipNumber).map(WalletArtifact::getApplePkpass).orElse(null);
  }

  @Override
  @Transactional(readOnly = true)
  public String getGoogleSaveUrl(String membershipNumber) {
    return repo.findById(membershipNumber).map(WalletArtifact::getGoogleSaveUrl).orElse(null);
  }

  // -------------------- Apple: build & sign pkpass --------------------
//  private byte[] buildApplePkpass(String membershipNumber, String memberName) throws Exception {
//    PKPass pass = PKPass.builder()
//            .formatVersion(1)
//            .passTypeIdentifier(passTypeId)
//            .serialNumber(UUID.randomUUID().toString()) // if you want stable serials, map & persist them
//            .teamIdentifier(teamId)
//            .organizationName(orgName)
//            .description(programName)
//            .logoText(orgName)
//            .webServiceURL(nullOrEmpty(webServiceUrl) ? null : new URL(webServiceUrl))
//            .authenticationToken(nullOrEmpty(authToken) ? null : authToken)
//            .barcodeBuilder(PKBarcode.builder()
//                    .format(PKBarcodeFormat.PKBarcodeFormatQR)
//                    .message(membershipNumber)
//                    .messageEncoding(StandardCharsets.UTF_8))
//            .pass(PKGenericPass.builder()
//                    .passType(PKPassType.PKStoreCard) // or PKGeneric
//                    .primaryFieldBuilder(PKField.builder()
//                            .key("member")
//                            .label("Member #")
//                            .value(membershipNumber))
//                    .secondaryFieldBuilder(PKField.builder()
//                            .key("name")
//                            .label("Name")
//                            .value(memberName == null ? "Member" : memberName))
//                    .build())
//            .build();
//
//    IPKPassTemplate template = new PKPassTemplateFolder(templateFolder);
//
//    PKSigningInformation info = new PKSigningInformationUtil()
//            .loadSigningInformationFromPKCS12AndIntermediateCertificate(
//                    passCertPath, passCertPwd, wwdrPath
//            );
//
//    PKFileBasedSigningUtil signer = new PKFileBasedSigningUtil();
//    return signer.createSignedAndZippedPkPassArchive(pass, template, info);
//  }

  private static boolean nullOrEmpty(String s) { return s == null || s.isBlank(); }

  // -------------------- Google: build Save URL (JWT) --------------------
  private String buildGoogleSaveUrlEmbedded(String membershipNumber, String memberName) throws Exception {
    // Loyalty Object JSON (embedded in the JWT). The Class (loyaltyClassId) must already exist.
    String objectId = issuerId + "." + membershipNumber;
    Map<String, Object> loyaltyObject = new LinkedHashMap<>();
    loyaltyObject.put("id", objectId);
    loyaltyObject.put("classId", loyaltyClassId);
    loyaltyObject.put("state", "active");
    loyaltyObject.put("accountId", membershipNumber);
    loyaltyObject.put("accountName", memberName == null ? "Member" : memberName);
    loyaltyObject.put("barcode", Map.of(
            "type", "qrCode",
            "value", membershipNumber
    ));

    // JWT payload
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("loyaltyObjects", List.of(loyaltyObject));

    // Build & sign JWT
    ServiceAccount sa = loadServiceAccount(Paths.get(gsaKeyPath));
    Algorithm alg = Algorithm.RSA256(null, sa.privateKey());

    String jwt = JWT.create()
            .withIssuer(sa.clientEmail())             // iss
            .withAudience("google")                   // aud
            .withClaim("typ", "savetowallet")        // required
            .withArrayClaim("origins", new String[] { /* optionally set your web origin */ })
            .withClaim("payload", payload)
            .sign(alg);

    return "https://pay.google.com/gp/v/save/" + jwt;
  }

  // ----------- Service Account loader (reads JSON key, extracts PKCS8 RSA) -----------
  private record ServiceAccount(String clientEmail, RSAPrivateKey privateKey) {}

  private static ServiceAccount loadServiceAccount(Path jsonKeyPath) throws Exception {
    String json = Files.readString(jsonKeyPath);
    @SuppressWarnings("unchecked")
    Map<String, Object> map = new Gson().fromJson(json, Map.class);
    String clientEmail = (String) map.get("client_email");
    String privateKeyPem = (String) map.get("private_key");
    RSAPrivateKey key = parsePkcs8RsaPrivateKey(privateKeyPem);
    return new ServiceAccount(clientEmail, key);
  }

  private static RSAPrivateKey parsePkcs8RsaPrivateKey(String pem) throws Exception {
    String clean = pem.replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s", "");
    byte[] der = Base64.getDecoder().decode(clean);
    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(der);
    return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(spec);
  }
}
