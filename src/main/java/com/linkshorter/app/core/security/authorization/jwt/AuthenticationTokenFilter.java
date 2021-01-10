package com.linkshorter.app.core.security.authorization.jwt;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class AuthenticationTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationTokenFilter.class);
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public AuthenticationTokenFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            log.debug("Starting filtering authorization token");
            Optional<String> jwtTokenFromRequest = extractJWTTokenFromRequest(request);

            if (jwtTokenFromRequest.isPresent() && jwtUtils.validateJwtToken(jwtTokenFromRequest.get())) {
                String username = jwtUtils.getUserNameFromJwtToken(jwtTokenFromRequest.get());
                log.debug("Fetch user login from token: {}", username);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("User with username: {} correctly authenticated with JWT token", username);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication with JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractJWTTokenFromRequest(HttpServletRequest request) {
        log.debug("Starting extracting token from request");
        Optional<String> jwtToken = Optional.empty();
        String authorizationHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = Optional.ofNullable(authorizationHeader.substring(7));
            log.debug("Token found inside 'Authorization' header");
        }
        return jwtToken;
    }
}