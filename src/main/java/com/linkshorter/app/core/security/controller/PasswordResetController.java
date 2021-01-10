package com.linkshorter.app.core.security.controller;

import com.google.gson.Gson;
import com.linkshorter.app.core.security.exception.UserNotFoundException;
import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.model.UserPasswordResetToken;
import com.linkshorter.app.core.security.service.UserService;
import com.linkshorter.app.features.email.service.EmailService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reset-password")
public class PasswordResetController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);
    private final UserService userService;
    private final EmailService emailService;
    private final Gson gson;

    public PasswordResetController(UserService userService, Gson gson,
                                   EmailService emailService) {
        this.userService = userService;
        this.gson = gson;
        this.emailService = emailService;
    }

    @ApiOperation(value = "Zresetowanie hasła użytkownika", notes = "Endpoint odpowiada za zresetowanie hasła użytkownika oraz wysłanie maila" +
            " z linkiem do resetowania hasła na email przypisany do użytkownika")
    @PostMapping("/send-password-reset-token")
    public ResponseEntity<?> resetPassword(@ApiParam(value = "Nazwa użytkownka", example = "someuser@gmail.com", required = true) @RequestBody String username) {
        ResponseEntity<?> responseEntity;
        try {
            User user = userService.findByUsername(username).orElseThrow(() -> new UserNotFoundException(username));
            userService.createUserPasswordResetToken(user);
            String emailSubject = "Resetowanie hasła w serwisie mylinkshorter.com";
            emailService.sendEmailToUser(user, emailSubject, "email-templates/reset-password.html");
        } catch (Exception exception) {
            String exceptionMessage = "Wystąpił błąd podczas wysyłania maila " +
                    "do resetowania hasła: " + exception.getMessage();
            log.error(exceptionMessage);
        }
        responseEntity = ResponseEntity.ok(gson.toJson("Mail wysłany"));
        return responseEntity;
    }

    @ApiOperation(value = "Walidacja tokenu do resetowania hasła", notes = "Endpoint odpowiada za walidację tokenu do zresetowania hasła")
    @GetMapping("/{id}/validate-password-reset-token")
    public ResponseEntity<Boolean> validateResetPasswordToken(@ApiParam(value = "token do resetowania hasła", required = true) @PathVariable("id") String token) {
        ResponseEntity<Boolean> responseEntity;
        try {
            UUID tokenId = UUID.fromString(token);
            boolean isUserPasswordResetTokenValid = userService.validateUserPasswordResetTokenById(tokenId);
            responseEntity = ResponseEntity.ok(Boolean.valueOf(isUserPasswordResetTokenValid));
        } catch (Exception exception) {
            String errorMessage = "Wystąpił problem podczas walidacji tokenu do resetu hasła: " + exception.getMessage();
            log.error(errorMessage);
            responseEntity = ResponseEntity.ok(Boolean.FALSE);
        }

        return responseEntity;
    }

    @ApiOperation(value = "Ustawienie nowego hasła dla użytkownika", notes = "Endpoint odpowiada za ustawienie nowego hasła dla użytkownika. " +
            "W przesyłanym obiekcie użytkownika wymagane jest jedynie hasło do podania")
    @PostMapping("/{id}/reset")
    public ResponseEntity<?> saveNewUserPasswordInDatabase(@ApiParam(value = "token do resetowania hasła", required = true) @PathVariable("id") UUID tokenId,
                                                           @ApiParam(value = "obiekt reprezentujący użytkownika", required = true) @RequestBody String newPassword) {
        ResponseEntity<?> responseEntity;
        try {
            boolean isUserPasswordResetTokenValid = userService.validateUserPasswordResetTokenById(tokenId);
            if (!isUserPasswordResetTokenValid) {
                throw new Exception("Token do resetowania hasła wygasł lub jest niepoprawny.");
            }
            Optional<UserPasswordResetToken> userPasswordResetTokenOptional = userService.findUserPasswordResetTokenById(tokenId);
            User userConnectedWithToken = userPasswordResetTokenOptional.get().getUser();
            userService.changeUserPassword(userConnectedWithToken, newPassword);
            userService.deleteUserPasswordResetTokenById(tokenId);

            responseEntity = ResponseEntity.ok(gson.toJson("Hasło zostało poprawnie zmienione. Możesz się zalogować."));
        } catch (Exception exception) {
            String errorMessage = "Wystąpił błąd podczas resetowania hasła: " + exception.getMessage();
            log.error(errorMessage);
            responseEntity = ResponseEntity.badRequest().body(gson.toJson(errorMessage));
        }
        return responseEntity;
    }
}
