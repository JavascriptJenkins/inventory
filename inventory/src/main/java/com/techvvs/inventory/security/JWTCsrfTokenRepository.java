package com.techvvs.inventory.security;//package com.isaac.collegeapp.security;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.web.csrf.CsrfToken;
//import org.springframework.security.web.csrf.CsrfTokenRepository;
//import org.springframework.security.web.csrf.DefaultCsrfToken;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.Arrays;
//import java.util.List;
//
//@Component
//public class JWTCsrfTokenRepository implements CsrfTokenRepository {
//
//    @Autowired
//    UserService userService;
//
//    @Autowired
//    JwtTokenProvider jwtTokenProvider;
//
//    private static final Logger log = LoggerFactory.getLogger(JWTCsrfTokenRepository.class);
//    private byte[] secret;
//
//    public JWTCsrfTokenRepository(byte[] secret) {
//        this.secret = secret;
//    }
//
//    @Override
//    public CsrfToken generateToken(HttpServletRequest request) {
//        System.out.println("------------> GENERATE CSRF TOKEN");
//        String token = jwtTokenProvider.resolveToken(request); //need to modify this to pull out the crf token
//        return new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", token);
//    }
//
//    @Override
//    public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
//
//        System.out.println("SAVING TOKEN: "+token.getToken());
//
//    }
//
//    @Override
//    public CsrfToken loadToken(HttpServletRequest request) {
//
//        if(request.getRequestURI().equals("/login/createSystemUser")){
//            System.out.println("############# creating system user - CSRF TOKEN SKIPPED #####################");
//            return new CsrfToken();
//
//        } else {
//
//            System.out.println("############# LOADING CSRF TOKEN #####################");
//
//            // String token = jwtTokenProvider.resolveToken(request); //need to modify this to pull out the crf token
//            //    String token3 = request.getParameter("_csrf"); //need to modify this to pull out the crf token
//            String token3 = jwtTokenProvider.resolveTokenFromCSFR(request);
//
//            if(token3== null || token3.length() <10){
//                System.out.println("############# CREATING DEFAULT TOKEN #####################");
//                //if we have no token, return a default one with no priveldges
//                String token1= jwtTokenProvider.createToken("default", (List<Role>) Arrays.asList(Role.ROLE_READ_ONLY));
//                return new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", token1);
//            }
//
//
//            return new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", token3);
//
//
//
//
//        }
//    }
//
//
//}
