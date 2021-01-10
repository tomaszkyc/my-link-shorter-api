package com.linkshorter.app.core.security.controller;


import com.google.gson.Gson;
import com.linkshorter.app.core.security.authorization.jwt.JwtUtils;
import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.model.dto.LoginFormDto;
import com.linkshorter.app.core.security.model.dto.RegisterUserFormDto;
import com.linkshorter.app.core.security.repository.UserRepository;
import com.linkshorter.app.core.security.service.UserService;
import com.linkshorter.app.features.email.service.EmailService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final EmailService emailService;
    private final JwtUtils jwtUtils;
    private final Gson gson;

    public AuthenticationController(AuthenticationManager authenticationManager,
                                    JwtUtils jwtUtils,
                                    Gson gson, UserService userService,
                                    EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.gson = gson;
        this.userService = userService;
        this.emailService = emailService;
    }

    @ApiOperation(value = "Uwierzytelnienie użytkownika", notes = "Należy podać informacje potrzebne do logowania (username i password)")
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@ApiParam(value = "obiekt przechowujący login i hasło użytkownika", required = true) @RequestBody LoginFormDto loginFormDto) {
        ResponseEntity<?> responseEntity;
        log.trace("Started authenticating user with login form data {}", loginFormDto);
        try {
            String username = loginFormDto.getUsername();
            String password = loginFormDto.getPassword();
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            log.trace("Username and password are valid. Setting authentication context");
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User userFetchedFromDatabase = userService.findByUsername(username).get();
            String jwtTokenForUser = jwtUtils.generateJwtTokenForUser(userFetchedFromDatabase);
            Map<String, Object> authenticationInformation = new HashMap<>();
            authenticationInformation.put("token", jwtTokenForUser);
            authenticationInformation.put("expiresAt", jwtUtils.getExpirationDateFromJwtToken(jwtTokenForUser));
            log.info("User with username: {} successfully authenticated", username);

            responseEntity = ResponseEntity.ok(gson.toJson(authenticationInformation));
        } catch (Exception userAuthenticationException) {
            String exceptionMessage = userAuthenticationException.getMessage();
            String jsonFormattedExceptionMessage = gson.toJson(exceptionMessage);
            responseEntity = ResponseEntity.badRequest().body(jsonFormattedExceptionMessage);
        }
        return responseEntity;
    }

    @ApiOperation(value = "Walidacja tokenu użytkownika", notes = "W body należy podać token użytkownika, który chcemy zwalidować")
    @PostMapping("/validate-user-token")
    public ResponseEntity<?> validateUserToken(@ApiParam(value = "token użytkownika", required = true) @RequestBody String userToken) {
        boolean isUserTokenValid = jwtUtils.validateJwtToken(userToken);
        return ResponseEntity.ok(gson.toJson(isUserTokenValid));
    }

    @ApiOperation(value = "Rejestracja użytkownika", notes = "Endpoint służy do rejestracji nowego użytkownika")
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@ApiParam(value = "obiekt reprezentujący informacje o użytkowniku", required = true) @RequestBody RegisterUserFormDto registerUserFormDto) {
        ResponseEntity<String> responseEntity;
        try {
            User userFromRegisterUserFormDto = registerUserFormDto.toUser();
            User newCreatedUser = userService.createUser(userFromRegisterUserFormDto);
            String subject = "Aktywacja konta w serwisie mylinkshorter.com";
            emailService.sendEmailToUser(newCreatedUser, subject, "email-templates/account-confirmation.html");
            String createdAccountMessage = "Konto utworzono pomyślnie. Na Twój adres email wysłaliśmy link aktywacyjny. Kliknij w niego, aby aktywować konto.";
            String jsonFormattedSuccessMessage = gson.toJson(createdAccountMessage);
            responseEntity = ResponseEntity.ok(jsonFormattedSuccessMessage);
        } catch (Exception userCreationException) {
            String errorMessage = userCreationException.getMessage();
            String jsonFormattedErrorMessage = gson.toJson(errorMessage);
            responseEntity = ResponseEntity.badRequest().body(jsonFormattedErrorMessage);
        }
        return responseEntity;
    }

    @ApiOperation(value = "Aktywacja konta użytkownika", notes = "Endpoint służy do aktywacji konta. " +
                                                                "Użytkownik zostaje na niego przekierowany po kliknięciu" +
                                                                " w link w mailu otrzymanym po rejestracji")
    @PostMapping("/activate-account")
    public ResponseEntity<Boolean> activateAccount(@ApiParam(value = "token aktywacyjny dla konta", required = true) @RequestBody String activationToken) {
        ResponseEntity<Boolean> responseEntity;
        try {
            UUID userId = UUID.fromString(activationToken);
            userService.activateUserAccount(userId);
            responseEntity = ResponseEntity.ok(Boolean.TRUE);
        } catch (Exception activateAccountException) {
            String errorMessage = activateAccountException.getMessage();
            log.error("Wystąpił błąd podczas aktywacji konta: {}", errorMessage);
            responseEntity = ResponseEntity.badRequest().body(Boolean.FALSE);
        }
        return responseEntity;
    }

    @ApiOperation(value = "Pobranie aktualnych uprawnień użytkownika", notes = "Endpoint służy do pobrania obecnych uprawnień użytkownika, który wysyła request.")
    @GetMapping("/authorities")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserAuthorities(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(user.getUserAuthorities());
    }
}
