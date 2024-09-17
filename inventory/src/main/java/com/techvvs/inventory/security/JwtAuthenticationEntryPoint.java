package com.techvvs.inventory.security;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         org.springframework.security.core.AuthenticationException authException) throws IOException, ServletException {

        // Forward the request to your custom HTML page for 401 Unauthorized
        RequestDispatcher dispatcher = request.getRequestDispatcher("/401");
        dispatcher.forward(request, response);
    }
}
