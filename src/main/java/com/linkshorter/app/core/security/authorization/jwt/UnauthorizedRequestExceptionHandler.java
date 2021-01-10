package com.linkshorter.app.core.security.authorization.jwt;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class UnauthorizedRequestExceptionHandler implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(UnauthorizedRequestExceptionHandler.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        String authorizationExceptionMessage = String.format("There was an unauthorized request. Details: %s", authException.getMessage());
        logger.error(authorizationExceptionMessage);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authorizationExceptionMessage);
    }

}