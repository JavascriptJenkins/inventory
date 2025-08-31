package com.techvvs.inventory.walletapi;

import com.techvvs.inventory.jparepo.CustomerRepo;
import com.techvvs.inventory.model.CustomerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.Optional;

/**
 * RESTful controller:
 *  - POST /wallet/memberships        -> create/issue pass resources for a membershipNumber
 *  - GET  /wallet/apple/{membershipNumber}.pkpass  -> download existing Apple pass (no creation)
 *  - GET  /wallet/google/{membershipNumber}        -> 302 to existing Google Save URL (no creation)
 *
 * Assumptions:
 *  - walletService.issueMembership(...) provisions and PERSISTS results keyed by membershipNumber.
 *  - walletService.getPkpass(membershipNumber) returns previously created .pkpass bytes or throws/returns null if missing.
 *  - walletService.getGoogleSaveUrl(membershipNumber) returns previously created Save URL or throws/returns null if missing.
 *
 * If you prefer a separate opaque issueId, swap membershipNumber for that id and return it from POST.
 */
@RestController
@RequestMapping("/wallet")
public class WalletController {

  @Autowired
  private CustomerRepo customerRepo;

  @Autowired
  private WalletService walletService;

  /** Request body for issuing passes. */
  public record IssueRequest(String membershipNumber) {}

  /** Response body returned after issuing. */
  public record IssueResponse(
          String membershipNumber,
          String appleDownloadUrl,
          String googleSaveUrl
  ) {}

  /**
   * Provision/issue wallet artifacts for a membership.
   * Creates/updates Google Wallet object and generates an Apple .pkpass, then persists them
   * so that subsequent GETs DO NOT create anything.
   */
  @PostMapping("/memberships")
  public ResponseEntity<IssueResponse> issue(@RequestBody IssueRequest req) throws Exception {
    if (req == null || req.membershipNumber() == null || req.membershipNumber().isBlank()) {
      return ResponseEntity.badRequest().build();
    }

    String membershipNumber = req.membershipNumber();
    Optional<CustomerVO> customerOpt = customerRepo.findByMembershipnumber(membershipNumber);
    if (customerOpt.isEmpty()) {
      // Not found in your system; choose 404 or 400 depending on your semantics
      return ResponseEntity.notFound().build();
    }
    CustomerVO c = customerOpt.get();

    // Create/ensure wallet artifacts; service should persist them keyed by membershipNumber
    WalletIssueResult result = walletService.issueMembership(
            membershipNumber,
            c.getName(),
            String.valueOf(c.getCustomerid())
    );

    // Build URLs that clients can hit later without creating state
    String appleUrl = "/wallet/apple/" + membershipNumber + ".pkpass";
    String googleUrl = result.googleAddToWalletUrl(); // already created by the service

    IssueResponse body = new IssueResponse(membershipNumber, appleUrl, googleUrl);

    // Location where the resource can be referenced; if you expose a GET for resource metadata,
    // point Location there. For now, point at the Apple download as a canonical handle.
    return ResponseEntity
            .created(URI.create("/wallet/memberships/" + membershipNumber))
            .body(body);
  }

  /**
   * Download previously issued Apple Wallet pass for this membership.
   * MUST NOT create anything. Return 404 if not already issued.
   */
  @GetMapping(value = "/apple/{membershipNumber}.pkpass", produces = "application/vnd.apple.pkpass")
  public ResponseEntity<byte[]> downloadApple(@PathVariable String membershipNumber) throws Exception {
    byte[] pkpass = walletService.getPkpass(membershipNumber);
    if (pkpass == null || pkpass.length == 0) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=membership.pkpass")
            .body(pkpass);
  }

  /**
   * Redirect to previously issued Google “Add to Wallet” URL for this membership.
   * MUST NOT create anything. Return 404 if not already issued.
   */
  @GetMapping("/google/{membershipNumber}")
  public ResponseEntity<Void> google(@PathVariable String membershipNumber) throws Exception {
    String url = walletService.getGoogleSaveUrl(membershipNumber);
    if (url == null || url.isBlank()) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.status(302).location(URI.create(url)).build();
  }
}
