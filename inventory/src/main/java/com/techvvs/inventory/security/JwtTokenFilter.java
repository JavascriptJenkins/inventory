package com.techvvs.inventory.security;

import com.techvvs.inventory.exception.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static com.techvvs.inventory.security.WhiteListUriConstants.*;

// We should use OncePerRequestFilter since we are doing a database call, there is no point in doing this more than once
public class JwtTokenFilter extends OncePerRequestFilter implements CsrfTokenRepository {

  private JwtTokenProvider jwtTokenProvider;

  public JwtTokenFilter(JwtTokenProvider jwtTokenProvider) {
    this.jwtTokenProvider = jwtTokenProvider;
  }

  String token = "";

  @Override
  protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                  HttpServletResponse httpServletResponse,
                                  FilterChain filterChain) throws ServletException, IOException {

      logger.info("REQUEST HIT JwtTokenFilter: "+httpServletRequest.getRequestURI());
      logger.info("JwtTokenFilter: "+httpServletRequest.getMethod());

      // cookie start
      try{
          Cookie[] cookies = httpServletRequest.getCookies();
          if (cookies != null) {
              Arrays.stream(cookies)
                      .filter(cookie -> CookieUtils.getJwtCookieName().equals(cookie.getName()))
                      .findFirst()
                      .ifPresent(cookie -> {
                          token = cookie.getValue();
                          if (jwtTokenProvider.validateToken(token, httpServletRequest, httpServletResponse)) {
                              //String username = jwtTokenProvider.getTokenSubject(token);
                              Authentication auth = jwtTokenProvider.getAuthentication(token);
                              SecurityContextHolder.getContext().setAuthentication(auth);
                          } else {
                              logger.info("token expired, logging it out. ");
                              logout(httpServletRequest,httpServletResponse);
                          }
                      });
          }
      } catch(Exception ex){
          handleException(ex,
                  filterChain,
                  httpServletRequest,
                  httpServletResponse);
      }
// cookie end



    try {
      if (token != null && jwtTokenProvider.validateToken(token, httpServletRequest, httpServletResponse)) {

      String tokenSubject = jwtTokenProvider.getTokenSubject(token);
      logger.info("TOKEN VALIDATED WITH SUBJECT: "+tokenSubject);


//        Authentication auth = jwtTokenProvider.getAuthentication(token);
//        SecurityContextHolder.getContext().setAuthentication(auth);

      } else if(httpServletRequest.getRequestURI().equals("/css/table.css")){
        logger.info("REQUEST FOR CSS");
      } else if(httpServletRequest.getRequestURI().equals("/login/verifyphonetoken") && "POST".equals(httpServletRequest.getMethod())){
          // post requests need a jwt token usually
          logger.info("DEBUG33333333");

      }else if(httpServletRequest.getRequestURI().equals("/file/upload") && "POST".equals(httpServletRequest.getMethod())){
          // post requests need a jwt token usually
          logger.info("DEBUG33333333");

      } else if(httpServletRequest.getRequestURI().equals("/customer/pipeline") && "POST".equals(httpServletRequest.getMethod())){
          // post requests need a jwt token usually
          logger.info("DEBUG35553333");

      } else if(httpServletRequest.getRequestURI().equals("/qr/publicinfo") && "GET".equals(httpServletRequest.getMethod())){
          // post requests need a jwt token usually
          logger.info("hit qr method");
          List<Role> roles = new ArrayList<>(1);
          roles.add(Role.PUBLIC_QR_ROLE);
          String publicToken = jwtTokenProvider.createTokenForPublicQR("johndoe@gmail.com", roles);
          Authentication auth = jwtTokenProvider.getAuthenticationForPublicQR(publicToken);
          SecurityContextHolder.getContext().setAuthentication(auth);
      } else if(httpServletRequest.getRequestURI().equals("/qr") && "GET".equals(httpServletRequest.getMethod())){
          // post requests need a jwt token usually
          logger.info("hit qr northstar method");
//          List<Role> roles = new ArrayList<>(1);
//          roles.add(Role.PUBLIC_QR_ROLE);
//          String publicToken = jwtTokenProvider.createTokenForPublicQR("johndoe@gmail.com", roles);
//          Authentication auth = jwtTokenProvider.getAuthenticationForPublicQR(publicToken);
//          SecurityContextHolder.getContext().setAuthentication(auth);
      }
    } catch (CustomException ex) {

        handleException(ex,
                filterChain,
                httpServletRequest,
                httpServletResponse);

    }

    filterChain.doFilter(httpServletRequest, httpServletResponse);
  }

  void handleException(Exception ex,
                       FilterChain filterChain,
                       HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse) throws ServletException, IOException {
      String path = httpServletRequest.getRequestURI();
      // Skip JWT validation for login endpoints etc
      if (LOGIN_SYSTEMUSER.equals(path) ||
              VERIFY_PHONE_TOKEN.equals(path) ||
              CREATE_ACCOUNT.equals(path) ||
              RESET_PASSWORD.equals(path) ||
              LOGIN.equals(path) ||
              LOGIN_REQUEST_LINK.equals(path) ||
              OAUTH_GOOGLE_CALLBACK.equals(path) ||
              OAUTH_GOOGLE_LOGIN.equals(path) ||
              OAUTH_GOOGLE_AUTH.equals(path) ||
              LOGIN_MAGIC_LINK_GATEWAY.equals(path) ||
              CREATE_SYSTEM_USER.equals(path) ||
              VERIFY.equals(path) ||
              QR.equals(path) ||
              LEGAL_TOS.equals(path) ||
              LEGAL_PRIVACY_POLICY.equals(path) ||
              PUBLIC_LANDING_PAGE_TULIP.equals(path) ||
              PUBLIC_CONFERENCE_PAGE_TULIP.equals(path) ||
              PUBLIC_CONFERENCE_PAGE_TULIP_POST.equals(path) ||
              FILE_SMS_DOWNLOAD.equals(path) ||
              FILE_SMS_DOWNLOAD_3.equals(path) ||
              FILE_SMS_DOWNLOAD_33.equals(path) ||
              FILE_QR_MEDIA_ZIP_DOWNLOAD.equals(path) ||
              FILE_QR_MEDIA_ZIP_DOWNLOAD_33.equals(path) ||
              VIDEO_VIDEOS.equals(path) ||
              VIDEO_VIDEOS_33.equals(path) ||
              VIDEO_PRODUCT.equals(path) ||
              VIDEO_PRODUCT_33.equals(path) ||
              PHOTO_PRODUCT.equals(path) ||
              PHOTO_PRODUCT_33.equals(path) ||
              PHOTO_PHOTO.equals(path) ||
              PHOTO_PHOTO_33.equals(path) ||
              DOCUMENT_PRODUCT.equals(path) ||
              DOCUMENT_PRODUCT_33.equals(path) ||
              DOCUMENT_DOCUMENTS.equals(path) ||
              DOCUMENT_DOCUMENTS_33.equals(path) ||
              IMAGE_IMAGES.equals(path) ||
              IMAGE_IMAGES_33.equals(path) ||
              IMAGE_IMAGES_PHOTOS.equals(path) ||
              IMAGE_IMAGES_PHOTOS_33.equals(path) ||
              MENU_URI_33.equals(path) ||
              MENU_URI.equals(path) ||
              MENU_SHOP_URI.equals(path) ||
              MENU_SHOP_URI_33.equals(path) ||
              MENU_URI_2_33.equals(path) ||
              MENU_URI_2.equals(path) ||
              MENU_SHOP_2_URI.equals(path) ||
              MENU_SHOP_URI_2_33.equals(path) ||
              MENU_URI_3_33.equals(path) ||
              MENU_URI_3.equals(path) ||
              MENU_URI_5.equals(path) ||
              MENU_URI_5_33.equals(path) ||
              MENU_SHOP_3_URI.equals(path) ||
              MENU_SHOP_URI_3_33.equals(path) ||
              FILE_SMS_DOWNLOAD_2.equals(path) ||
              FILE_SMS_DOWNLOAD_22.equals(path) ||
              FILE_PUBLIC_DOWNLOAD.equals(path) ||
              FILE_PUBLIC_DOWNLOAD_2.equals(path) ||
              DELIVERY_ITEM.equals(path) ||
              DELIVERY_ITEM_33.equals(path) ||
              KALE_MOVIE.equals(path) ||
              KALE_MOVIE_33.equals(path) ||
              PAYPAL_CAPTURE_API_1.equals(path) ||
              PAYPAL_CAPTURE_API_1_33.equals(path) ||
              PAYPAL_THANKYOU_API_1_33.equals(path) ||
              PAYPAL_THANKYOU_API_1.equals(path) ||
              PAYPAL_CANCEL_API_1_33.equals(path) ||
              PAYPAL_CANCEL_API_1.equals(path) ||
              PAYPAL_RETURN_API_1_33.equals(path) ||
              PAYPAL_RETURN_API_1.equals(path) ||
              PAYPAL_JSON_API_1_33.equals(path) ||
              PAYPAL_JSON_API_1.equals(path) ||
              MCP_API.equals(path)
      )
      {
          filterChain.doFilter(httpServletRequest, httpServletResponse);
          return;
      }

      logger.info("Exception on JWT controller: "+ex.getMessage());
      throw new BadCredentialsException(ex.getMessage(), ex);

  }

  @Override
  public CsrfToken generateToken(HttpServletRequest httpServletRequest) {
    System.out.println("############# GENERATE CSRF TOKEN #####################");
    return null;
  }

  @Override
  public void saveToken(CsrfToken csrfToken, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    System.out.println("############# SAVE CSRF TOKEN #####################");
  }

  @Override
  public CsrfToken loadToken(HttpServletRequest httpServletRequest) {


        System.out.println("############# LOADING CSRF TOKEN #####################");

        // String token = jwtTokenProvider.resolveToken(request); //need to modify this to pull out the crf token
        //    String token3 = request.getParameter("_csrf"); //need to modify this to pull out the crf token
        String token3 = jwtTokenProvider.resolveTokenFromCSFR(httpServletRequest);

        if(token3== null || token3.length() <10){
            System.out.println("############# CREATING DEFAULT TOKEN #####################");
            //if we have no token, return a default one with no priveldges
            String token1= jwtTokenProvider.createToken("default", (List<Role>) Arrays.asList(Role.ROLE_READ_ONLY));
            return new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", token1);
        }


        return new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", token3);


  }


    @Autowired
    CookieUtils cookieUtils;

    void logout(HttpServletRequest request, HttpServletResponse response){

        // Invalidate the session if it exists
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear authentication information from the SecurityContext
        SecurityContextHolder.clearContext();

        // Wrap response to add SameSite attribute automatically
        SameSiteCookieResponseWrapper wrappedResponse = cookieUtils.wrapResponse(response);
        
        // Remove the JWT cookie using utility
        Cookie cookie = cookieUtils.createLogoutCookie();
        wrappedResponse.addCookie(cookie);

        SecurityContextHolder.getContext().setAuthentication(null); // clear the internal auth
        SecurityContextHolder.clearContext();
    }
}
