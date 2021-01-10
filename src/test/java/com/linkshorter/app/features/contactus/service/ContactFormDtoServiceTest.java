package com.linkshorter.app.features.contactus.service;

import com.linkshorter.app.features.contactus.model.ContactFormDto;
import com.linkshorter.app.features.email.service.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ContactFormDtoServiceTest {

    private ContactFormDtoService contactFormDtoService;
    private ContactFormDto contactFormDto;
    @Autowired
    private EmailService emailService;
    @BeforeEach
    void setUp() {
        if (contactFormDtoService == null) {
            contactFormDtoService = new ContactFormDtoService(emailService);
        }
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    public void shouldThrowExceptionIfValidatingNullForm() {
        contactFormDto = null;
        assertThrows(Exception.class, () -> contactFormDtoService.validate(contactFormDto));
    }

    @Test
    public void shouldThrowAnExceptionIfOneOfParametersIsEmpty() {
        contactFormDto = new ContactFormDto();
        assertThrows(Exception.class, () -> contactFormDtoService.validate(contactFormDto));
        contactFormDto.setEmail("some-email@gmail.com");
        assertThrows(Exception.class, () -> contactFormDtoService.validate(contactFormDto));
        contactFormDto.setName("Some mail");
        assertThrows(Exception.class, () -> contactFormDtoService.validate(contactFormDto));
        contactFormDto.setMessage("Some form message");
        contactFormDto.setCreationDate(null);
        assertThrows(Exception.class, () -> contactFormDtoService.validate(contactFormDto));
        contactFormDto.setCreationDate(new Date());
        assertDoesNotThrow(() -> contactFormDtoService.validate(contactFormDto));
    }

    @Test
    public void shouldNotThrowExceptionIfFormIsFilledCorrectly() {
        contactFormDto = new ContactFormDto();
        contactFormDto.setEmail("some-mail@gmail.com");
        contactFormDto.setName("Some name");
        contactFormDto.setMessage("Some message");

        assertDoesNotThrow(() -> contactFormDtoService.validate(contactFormDto));
    }

    @Test
    public void shouldSendEmail() {
        contactFormDto = new ContactFormDto();
        contactFormDto.setEmail("some-mail@gmail.com");
        contactFormDto.setName("Some name");
        contactFormDto.setMessage("Some message");

        assertDoesNotThrow(() -> contactFormDtoService.sendContactFormDtoByMail(contactFormDto));
    }

    @Test
    public void shouldThrowExceptionIfSendingEmailWithInvalidForm() {
        contactFormDto = new ContactFormDto();
        assertThrows(Exception.class, () -> contactFormDtoService.validate(contactFormDto));
    }
}