package com.techvvs.inventory.viewcontroller.helper

import com.techvvs.inventory.security.CookieUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

@Component
class AuthHelper {

    @Autowired
    CookieUtils cookieUtils


    void logout(HttpServletRequest request, HttpServletResponse response){

        // Invalidate the session if it exists
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear authentication information from the SecurityContext
        SecurityContextHolder.clearContext();

        // Wrap response to add SameSite attribute automatically
        def wrappedResponse = cookieUtils.wrapResponse(response);
        
        // Remove the JWT cookie using utility
        Cookie cookie = cookieUtils.createLogoutCookie();
        wrappedResponse.addCookie(cookie);


        SecurityContextHolder.getContext().setAuthentication(null); // clear the internal auth
        SecurityContextHolder.clearContext();
    }

}
