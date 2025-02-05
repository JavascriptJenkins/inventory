package com.techvvs.inventory.service.auth

import com.techvvs.inventory.jparepo.TokenRepo
import com.techvvs.inventory.model.TokenDAO
import com.techvvs.inventory.security.JwtTokenProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.ui.Model

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Service
class TechvvsAuthService {


    @Autowired
    JwtTokenProvider jwtTokenProvider

    @Autowired
    TokenRepo tokenRepo

    void checkuserauth(Model model){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();

        model.addAttribute("isAuthenticated", isAuthenticated);
    }

    void updateJwtToken(String token, HttpServletResponse response){
        Authentication auth = jwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Set the updated JWT token in a cookie
        Cookie jwtCookie = new Cookie("techvvs_token", token);
        jwtCookie.setHttpOnly(true); // Secure the cookie
        jwtCookie.setSecure(true);  // Ensure it's sent only over HTTPS
        jwtCookie.setPath("/");    // Make it available to the entire application
        jwtCookie.setMaxAge(1 * 24 * 60 * 60); // Set expiration time (e.g., 1 day)

        // Add the cookie to the response
        response.addCookie(jwtCookie);
    }

    String getActiveCookie(HttpServletRequest request){
        Cookie[] cookies = request.getCookies();

        String token = ""
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("techvvs_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        }
        return token;
    }

    boolean isTokenUsed(String token){
        Optional<TokenDAO> tokenDao = tokenRepo.findByToken(token)
        if(tokenDao.isEmpty()){
            return true // this should never happen but if it does just return true
        }
        if (tokenDao.present && tokenDao.get().tokenused == 1) {
            return true
        }
        if(tokenDao.present && tokenDao.get().tokenused == 0){
            return false
        }

        return true
    }


}
