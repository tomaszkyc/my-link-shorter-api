package com.linkshorter.app.features.contactus.controller;


import com.google.gson.Gson;
import com.linkshorter.app.features.contactus.model.ContactFormDto;
import com.linkshorter.app.features.contactus.service.ContactFormDtoService;
import com.linkshorter.app.features.users.controller.UserController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contact-us")
public class ContactUsController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final ContactFormDtoService contactFormDtoService;
    private final Gson gson;

    public ContactUsController(ContactFormDtoService contactFormDtoService, Gson gson) {
        this.contactFormDtoService = contactFormDtoService;
        this.gson = gson;
    }

    @PostMapping
    public ResponseEntity<String> sendContactFormDto(@RequestBody ContactFormDto contactFormDto) {
        ResponseEntity<String> responseEntity;
        try {
            contactFormDtoService.sendContactFormDtoByMail(contactFormDto);
            String successSendMessage = "Wiadomość została wysłana. Skontaktujemy się z Tobą tak szybko jak to możliwe.";
            responseEntity = ResponseEntity.ok(gson.toJson(successSendMessage));
        } catch (Exception sendContactFormDtoException) {
            String exceptionMessage = sendContactFormDtoException.getMessage();
            log.error(exceptionMessage);
            String jsonConvertedExceptionMessage = gson.toJson(exceptionMessage);
            responseEntity = ResponseEntity.badRequest().body(jsonConvertedExceptionMessage);
        }
        return responseEntity;
    }


}
