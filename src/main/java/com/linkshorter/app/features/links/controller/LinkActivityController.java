package com.linkshorter.app.features.links.controller;

import com.google.gson.Gson;
import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.features.links.service.LinkActivityService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/link-activity")
public class LinkActivityController {

    private static final Logger log = LoggerFactory.getLogger(LinkActivityController.class);
    private final Gson gson;
    private final LinkActivityService linkActivityService;

    public LinkActivityController(Gson gson, LinkActivityService linkActivityService) {
        this.gson = gson;
        this.linkActivityService = linkActivityService;
    }

    @ApiOperation(value = "Pobranie aktywności dla zadanego id linku", notes = "Endpoint odpowiada za pobranie wszystkich aktywności dla " +
            "podanego linku, reprezentowanego jako id")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllLinkActivities(@ApiParam(value = "id linku", required = true) @PathVariable(name = "id") UUID linkId, @AuthenticationPrincipal User user) {
        ResponseEntity<?> responseEntity;
        try {
            responseEntity = ResponseEntity.ok(this.linkActivityService.findAllByLinkIdAndUser(linkId, user));
        } catch (Exception exception) {
            String exceptionMessage = exception.getMessage();
            log.error("There was an error on getting all link actiities for linkId {}. Error message: {} ", linkId, exceptionMessage);
            responseEntity = ResponseEntity.badRequest().body(gson.toJson(exceptionMessage));
        }
        return responseEntity;
    }
}
