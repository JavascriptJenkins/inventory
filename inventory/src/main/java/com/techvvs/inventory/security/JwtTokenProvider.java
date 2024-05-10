package com.techvvs.inventory.security;

import com.techvvs.inventory.exception.CustomException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  /**
   * THIS IS NOT A SECURE PRACTICE! For simplicity, we are storing a static key here. Ideally, in a
   * microservices environment, this key would be kept on a config-server.
   */

  // https://stackoverflow.com/questions/35238579/password-encoding-and-decoding-using-spring-security-spring-boot-and-mongodb
  @Value("${security.jwt.token.secret-key:secret-key}") // todo: change this "secret-key" value to a real hash - see link above
  private String secretKey;

  @Value("${security.jwt.token.expire-length:86400000}")
  private long validityInMilliseconds = 86400000; // 24 hour


    @Value("${security.jwt.token.expire-length:86400000}")
    private long validityInMillisecondsEmailValidation = 86400000; // 24 hour

  @Value("${security.jwt.token.expire-length:86400000}")
  private long validityInMillisecondsPhoneDownload = 3600000 ; // 1 hour

  @Autowired
  private MyUserDetails myUserDetails;

//  @Autowired
//  JWTCsrfTokenRepository jwtCsrfTokenRepository;

  @PostConstruct
  protected void init() {
    secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
  }

  // this should probably be signed with a different key than regular user tokens
    public String createTokenForEmailValidation(String email, List<Role> roles) {

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("auth", roles.stream().map(s -> new SimpleGrantedAuthority(s.getAuthority())).filter(Objects::nonNull).collect(Collectors.toList()));

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMillisecondsEmailValidation);

        return Jwts.builder()//
                .setClaims(claims)//
                .setIssuedAt(now)//
                .setExpiration(validity)//
                .signWith(SignatureAlgorithm.HS256, secretKey)//
                .compact();
    }

  // this should probably be signed with a different key than regular user tokens
  public String createTokenForSmsDownloadLinks(String email, List<Role> roles) {

    Claims claims = Jwts.claims().setSubject(email);
    claims.put("auth", roles.stream().map(s -> new SimpleGrantedAuthority(s.getAuthority())).filter(Objects::nonNull).collect(Collectors.toList()));

    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMillisecondsPhoneDownload);

    return Jwts.builder()//
            .setClaims(claims)//
            .setIssuedAt(now)//
            .setExpiration(validity)//
            .signWith(SignatureAlgorithm.HS256, secretKey)//
            .compact();
  }


  public String createToken(String username, List<Role> roles) {

    Claims claims = Jwts.claims().setSubject(username);
    claims.put("auth", roles.stream().map(s -> new SimpleGrantedAuthority(s.getAuthority())).filter(Objects::nonNull).collect(Collectors.toList()));

    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()//
        .setClaims(claims)//
        .setIssuedAt(now)//
        .setExpiration(validity)//
        .signWith(SignatureAlgorithm.HS256, secretKey)//
        .compact();
  }


  public String createTokenForLogin(String username, List<Role> roles, List<String> orgs){
    Claims claims = Jwts.claims().setSubject(username);
    claims.put("auth", roles.stream().map(s -> new SimpleGrantedAuthority(s.getAuthority())).filter(Objects::nonNull).collect(Collectors.toList()));
    claims.put("orgs", orgs);

    Date now = new Date();
    Date validity = new Date(now.getTime() + validityInMilliseconds);

    return Jwts.builder()//
            .setClaims(claims)//
            .setIssuedAt(now)//
            .setExpiration(validity)//
            .signWith(SignatureAlgorithm.HS256, secretKey)//
            .compact();
  }


  public Authentication getAuthentication(String token) {
    UserDetails userDetails = myUserDetails.loadUserByUsername(getTokenSubject(token));
    return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
  }

  public String getTokenSubject(String token) {
    return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
  }

  public String resolveTokenFromCookies(HttpServletRequest req) {

    Cookie[] cookelist = req.getCookies();

    if(cookelist != null){
      Cookie cookie = cookelist[0];

      String bearerToken = cookie.getValue();
      if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
        return bearerToken.substring(7);
      }

    }
    return null;
  }



  public String resolveToken(HttpServletRequest req) {
    String bearerToken = req.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

//  public String resolveTokenFromHiddenCustomParam(HttpServletRequest req) {
//
//    Object rerqatt = req.getAttribute("customJwtParameter");
//
//    return null;
//    // return String.valueOf(req.getAttribute("_csrf"));
//  }

  public String resolveTokenFromCSFR(HttpServletRequest req) {

    Object rerqatt = req.getAttribute("_csrf");
    if(rerqatt != null){
      return ((CsrfToken) rerqatt).getToken();
    }

    return null;
   // return String.valueOf(req.getAttribute("_csrf"));
  }

  public String resolveTokenFromWebSocket(HttpServletRequest req) {
    if(req.getQueryString().contains("jwt")){
      return req.getQueryString().substring(4);
    } else {
      return "";
    }
  }

  public boolean validateToken(String token) {
    try {
      Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      throw new CustomException("Expired or invalid JWT token", HttpStatus.FORBIDDEN);
    }
  }

}
