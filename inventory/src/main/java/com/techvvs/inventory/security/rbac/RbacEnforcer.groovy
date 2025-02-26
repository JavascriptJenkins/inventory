package com.techvvs.inventory.security.rbac

import com.techvvs.inventory.security.JwtTokenProvider
import com.techvvs.inventory.security.Role
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Component
import org.springframework.ui.Model

import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

/* This class is for enforcing RBAC control to resources */
@Component
class RbacEnforcer {

    @Autowired
    JwtTokenProvider jwtTokenProvider


    boolean enforceAdminRights(Model model, HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        String token = ""
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("techvvs_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                }
            }
        } else {
            return false // return false if cookies is null
        }

        List<String> authorities = jwtTokenProvider.extractAuthorities(token) // passing internal token in here
        if(hasRole(authorities, String.valueOf(Role.ROLE_ADMIN))){
            // write code here to add certain attributes to the model that will enable the user to click certain buttons
            model.addAttribute("AdminViewActivated", "yes")
            return true
        }
        return false
    }

    boolean hasRole(List authorities, String roleToCheck) {
        println "Authorities: ${authorities}"
        println "Role to check: ${roleToCheck}"

        return authorities.any { authority ->
            def valueToCompare = authority instanceof GrantedAuthority ? authority.authority : authority.toString()
            valueToCompare == roleToCheck
        }
    }



}
