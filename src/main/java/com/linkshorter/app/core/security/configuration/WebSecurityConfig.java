package com.linkshorter.app.core.security.configuration;

import com.linkshorter.app.core.security.authorization.jwt.AuthenticationTokenFilter;
import com.linkshorter.app.core.security.authorization.jwt.JwtUtils;
import com.linkshorter.app.core.security.authorization.jwt.UnauthorizedRequestExceptionHandler;
import com.linkshorter.app.core.security.repository.UserRepository;
import com.linkshorter.app.core.security.service.UserRepositoryUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final UnauthorizedRequestExceptionHandler unauthorizedHandler;
    private final JwtUtils jwtUtils;


    public WebSecurityConfig(UserDetailsService userDetailsService, UnauthorizedRequestExceptionHandler unauthorizedHandler, JwtUtils jwtUtils) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtUtils = jwtUtils;
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository users) {
        return new UserRepositoryUserDetailsService(users);
    }

    @Bean
    public AuthenticationTokenFilter authenticationJwtTokenFilter() {
        return new AuthenticationTokenFilter(jwtUtils, userDetailsService);
    }

    @Override
    public void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeRequests()
                .antMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/validate-user-token").permitAll()
                .antMatchers(HttpMethod.POST, "/api/v1/auth/activate-account").permitAll()
                .antMatchers(HttpMethod.POST, "/api/v1/link").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/link/{shortLink}/fetch-user-data").permitAll()
                .antMatchers(HttpMethod.POST,"/api/v1/reset-password/send-password-reset-token").permitAll()
                .antMatchers(HttpMethod.GET, "/api/v1/reset-password/{id}/validate-password-reset-token").permitAll()
                .antMatchers(HttpMethod.POST, "/api/v1/reset-password/{id}/reset").permitAll()
                .antMatchers(HttpMethod.POST, "/api/v1/contact-us").permitAll()
                .antMatchers("/v2/api-docs", "/configuration/**", "/swagger*/**", "/webjars/**").permitAll()
                .anyRequest().authenticated();

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
