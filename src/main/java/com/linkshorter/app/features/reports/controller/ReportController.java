package com.linkshorter.app.features.reports.controller;

import com.google.gson.Gson;
import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.service.UserService;
import com.linkshorter.app.features.reports.model.GroupBy;
import com.linkshorter.app.features.reports.service.ReportService;
import com.linkshorter.app.features.users.controller.UserController;
import org.apache.coyote.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final Gson gson;
    private final ReportService reportService;

    public ReportController(UserService userService, Gson gson, ReportService reportService) {
        this.userService = userService;
        this.gson = gson;
        this.reportService = reportService;
    }

    @GetMapping("/group-by-parameters")
    @PreAuthorize("hasAnyAuthority('admin', 'premium-user')")
    public ResponseEntity<?> getGroupByParameters() {
        ResponseEntity<?> responseEntity = null;
        responseEntity = ResponseEntity.ok(gson.toJson(GroupBy.generateValues()));
        return responseEntity;
    }

    @PostMapping("/generate")
    @PreAuthorize("hasAnyAuthority('admin', 'premium-user')")
    public ResponseEntity<?> generateReport(@RequestBody Map<String, Object> reportParameters, @AuthenticationPrincipal User user) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        ResponseEntity<?> responseEntity;
        String groupByValue = (String)reportParameters.get("groupBy");
        try {
            Date dateFrom = simpleDateFormat.parse((String)reportParameters.get("dateFrom"));
            Date dateTo = simpleDateFormat.parse((String)reportParameters.get("dateTo"));
            GroupBy groupBy = GroupBy.parse(groupByValue);
            responseEntity = ResponseEntity.ok(reportService.generateGroupByReport(groupBy, dateFrom, dateTo, user));
        } catch (Exception exception) {
            String errorMessage = "Wystapił problem podczas generowania raportu: " + exception.getMessage();
            log.error(errorMessage);
            responseEntity = ResponseEntity.badRequest().body(gson.toJson(errorMessage));
        }
        return responseEntity;
    }

}
