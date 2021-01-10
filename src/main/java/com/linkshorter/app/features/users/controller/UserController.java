package com.linkshorter.app.features.users.controller;


import com.google.gson.Gson;
import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final Gson gson;

    public UserController(UserService userService, Gson gson) {
        this.userService = userService;
        this.gson = gson;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<List<User>> getAll() {
        ResponseEntity<List<User>> responseEntity;
        responseEntity = ResponseEntity.ok(userService.findAll());
        return responseEntity;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> get(@PathVariable(name = "id") UUID userId) {
        ResponseEntity<?> responseEntity;
        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            responseEntity = ResponseEntity.ok(user.get());
        } else {
            String errorMessage = "Użytkownik o id: " + userId + " nie istnieje.";
            log.error(errorMessage);
            responseEntity = ResponseEntity.badRequest().body(gson.toJson(errorMessage));
        }
        return responseEntity;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<?> update(@PathVariable(name = "id") UUID userId, @RequestBody User userWithNewDetails) {
        ResponseEntity<?> responseEntity;
        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            try {
                responseEntity = ResponseEntity.ok(userService.updateUser(userWithNewDetails));
            } catch (Exception e) {
                String errorMessage = "Wystąpił problem podczas aktualizacji " +
                        "użytkownika " + userId + " :" + e.getMessage();
                log.error(errorMessage);
                responseEntity = ResponseEntity.badRequest().body(gson.toJson(errorMessage));
            }
        }
        else {
            String errorMessage = "Wystapił problem podczas aktualizacji użytkownika " + userId
                     + " : użytkownik o podanym id nie istnieje.";
            log.error(errorMessage);
            responseEntity = ResponseEntity.badRequest().body(gson.toJson(errorMessage));
        }
        return responseEntity;
    }

}
