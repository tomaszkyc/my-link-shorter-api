package com.linkshorter.app.features.links.controller;

import com.google.gson.Gson;
import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.features.links.model.Link;
import com.linkshorter.app.features.links.model.LinkActivity;
import com.linkshorter.app.features.links.service.LinkActivityService;
import com.linkshorter.app.features.links.service.LinkService;
import com.linkshorter.app.features.links.validator.LinkValidator;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/link")
public class LinkController {

    private static final Logger log = LoggerFactory.getLogger(LinkController.class);
    private final LinkService linkService;
    private final LinkActivityService linkActivityService;
    private final Gson gson;

    public LinkController(LinkService linkService, Gson gson, LinkActivityService linkActivityService) {
        this.linkService = linkService;
        this.gson = gson;
        this.linkActivityService = linkActivityService;
    }

    @ApiOperation(value = "Pobranie danych o osobie wywołującej krótki link i zwrócenie długiego linku do przekierowania",
                  notes = "Endpoint odpowiada za pobranie danych o użytkowniku wywołującym link. W odpowiedzi zwraca obiekt link")
    @GetMapping("/{shortLink}/fetch-user-data")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getLinkAndFetchUserAgentData(@ApiParam(value = "krótki link", example = "Kd8J4xc", required = true) @PathVariable(name = "shortLink") String shortLink, @ApiParam(hidden = true) @RequestHeader(name = "User-Agent") String userAgentHeader) {
        ResponseEntity<?> responseEntity;
        log.debug("User agent header: {}", userAgentHeader);
        Optional<Link> optionalLink = linkService.findByShortLinkAndActiveIsTrueAAndExpirationDateBeforeNow(shortLink);
        if (optionalLink.isEmpty()) {
            responseEntity = ResponseEntity.badRequest().body(gson.toJson("Nie udało się znaleźć krótkiego linku, którego szukasz." +
                    "Sprawdź, czy posiadany krótki link jest poprawny i spróbuj ponownie."));
        }
        else {
            Link link = optionalLink.get();
            responseEntity = ResponseEntity.ok(link);
            Date activityDate = new Date();
            linkActivityService.processUserAgentHeaderAsync(userAgentHeader, link, activityDate);
        }
        log.debug("Returned response to user");
        return responseEntity;
    }

    @ApiOperation(value = "Utworzenie nowego linku", notes = "Endpoint odpowiada za utworzenie nowego linku")
    @PostMapping
    public ResponseEntity<?> createLink(@ApiParam(value = "obiekt reprezentujący link", required = true) @RequestBody Link link, @AuthenticationPrincipal User user) {
        ResponseEntity<?> responseEntity;
        if (user != null) {
            link.setUser(user);
        }

        try {
            responseEntity = ResponseEntity.ok(this.linkService.createLink(link));
        } catch (Exception exception) {
            String exceptionMessage = exception.getMessage();
            log.error("There was an error on link creation: " + exceptionMessage);
            responseEntity = ResponseEntity.badRequest().body(gson.toJson(exceptionMessage));
        }
        return responseEntity;
    }

    @ApiOperation(value = "Pobranie wszystkich linków zalogowanego użytkownika", notes = "Pobranie wszystkich linków zalogowanego użytkownika / użytkownika wywołującego request")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Link>> getAllLinks(@AuthenticationPrincipal User user) {
        ResponseEntity<List<Link>> responseEntity;
        List<Link> links = this.linkService.findAllByUser(user);
        responseEntity = ResponseEntity.ok(links);
        return responseEntity;
    }

    @ApiOperation(value = "Usunięcie linku po id", notes = "Endpoint odpowiada za usunięcie linku po zadanym id.")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteLink(@ApiParam(value = "id linku do usunięcia", required = true) @PathVariable(name = "id") UUID linkId, @AuthenticationPrincipal User user) {
        ResponseEntity<?> responseEntity;
        try {
            Optional<Link> optionalLink = this.linkService.findByIdAndUser(linkId, user);
            if (optionalLink.isPresent()) {
                Link link = optionalLink.get();
                this.linkService.deleteLink(link, user);
            }
        } catch (Exception exception) {
            String exceptionMessage = exception.getMessage();
            log.error("There was an error on link deletion: " + exceptionMessage);
            responseEntity = ResponseEntity.badRequest().body(gson.toJson(exceptionMessage));
        }
        responseEntity = ResponseEntity.ok(gson.toJson("Link o id: " + linkId + " poprawnie usunięty"));
        return responseEntity;
    }

    @ApiOperation(value = "Pobranie linku po id", notes = "Endpoint odpowiada za pobranie linku po jego unikalnym id.")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getLink(@ApiParam(value = "unikalne id linku", required = true) @PathVariable(name = "id") UUID linkId, @AuthenticationPrincipal User user) {
        ResponseEntity<?> responseEntity;
        Optional<Link> link = this.linkService.findByIdAndUser(linkId, user);
        if (link.isEmpty()) {
            responseEntity = ResponseEntity.badRequest().body(gson.toJson("Nie masz uprawnień do pobrania szczegółów linku o id: " + linkId));
        }
        else {
            responseEntity = ResponseEntity.ok(link.get());
        }
        return responseEntity;
    }

    @ApiOperation(value = "Aktualizacja linku", notes = "Endpoint odpowiada za aktualizację linku")
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateLink(@ApiParam(value = "unikalne id linku", required = true) @PathVariable(name = "id") UUID linkId,
                                        @ApiParam(value = "obiekt reprezentujący nowe dane linku", required = true) @RequestBody Link newLinkData,
                                        @AuthenticationPrincipal User user) {
        ResponseEntity<?> responseEntity;
        try {
            responseEntity = ResponseEntity.ok(this.linkService.updateLink(linkId, newLinkData, user));
        } catch (Exception exception) {
            String exceptionCause = exception.getMessage();
            log.error("There was an error on link update: " + exceptionCause);
            responseEntity = ResponseEntity.badRequest().body(gson.toJson(exceptionCause));
        }
        return responseEntity;
    }

}
