package com.techvvs.inventory.viewcontroller.helper

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

@Component
class AuthHelper {


    void logout(HttpServletRequest request, HttpServletResponse response){

        // Invalidate the session if it exists
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear authentication information from the SecurityContext
        SecurityContextHolder.clearContext();

        // Remove the JWT cookie
        Cookie cookie = new Cookie("techvvs_token", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // Deletes the cookie
        response.addCookie(cookie);


        SecurityContextHolder.getContext().setAuthentication(null); // clear the internal auth
        SecurityContextHolder.clearContext();
    }

}
