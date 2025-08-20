package com.self.userauth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;


/*
 * this will send HTTP 401 Unauthorized with the JSON whenever authentication fails.
 * */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        PrintWriter writer = response.getWriter();
        writer.write("{\"error\": \"Unauthorized\", \"message\": \"Token is missing or invalid\"}");
        writer.flush();
        writer.close();
    }
}
