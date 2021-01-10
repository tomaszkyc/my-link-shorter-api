package com.linkshorter.app.features.usersettings.controller;

import com.google.gson.Gson;
import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.features.usersettings.model.UserSettingsDto;
import com.linkshorter.app.features.usersettings.service.UserSettingsDtoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user-settings")
public class UserSettingsController {

    private static final Logger log = LoggerFactory.getLogger(UserSettingsController.class);
    private final Gson gson;
    private final UserSettingsDtoService userSettingsDtoService;

    public UserSettingsController(Gson gson,
                                  UserSettingsDtoService userSettingsDtoService) {
        this.gson = gson;
        this.userSettingsDtoService = userSettingsDtoService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getUserDetails(@AuthenticationPrincipal User user) {
        ResponseEntity<?> responseEntity;
        try {
            UserSettingsDto userSettingsDto = UserSettingsDto.builder()
                    .email(user.getEmail())
                    .fullname(user.getFullName())
                    .userAdditionalProperties(user.getUserAdditionalPropertiesAsMap())
                    .build();
            responseEntity = ResponseEntity.ok(gson.toJson(userSettingsDto));
        } catch (Exception exception) {
            String exceptionMessage = exception.getMessage();
            log.error("There was an error on fetching user details: " + exceptionMessage);
            responseEntity = ResponseEntity.badRequest().body(gson.toJson(exceptionMessage));
        }
        return responseEntity;
    }

    @PutMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateUserDetails(@RequestBody UserSettingsDto userSettingsDto, @AuthenticationPrincipal User user) {
        ResponseEntity<?> responseEntity;
        try {
            userSettingsDtoService.updateUserDetails(userSettingsDto, user);
            responseEntity = ResponseEntity.ok(gson.toJson("Dane użytkownika poprawnie zaktualizowane"));
        } catch (Exception exception) {
            String exceptionMessage = exception.getMessage();
            log.error("There was an error on updating user details: " + exceptionMessage);
            responseEntity = ResponseEntity.badRequest().body(gson.toJson(exceptionMessage));
        }
        return responseEntity;
    }
}
