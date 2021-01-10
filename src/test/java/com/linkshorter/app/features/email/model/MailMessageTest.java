package com.linkshorter.app.features.email.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.MimeMessagePreparator;

import javax.naming.ldap.HasControls;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


class MailMessageTest {

    private static final String TO = "some_address@gmail.com";
    private static final String SUBJECT = "Some subject";
    private static final String TEXT = "Some text";
    private MailMessage mailMessage;

    @BeforeEach
    void setUp() {

    }

    @AfterEach
    void tearDown() {
        mailMessage = null;
    }

    @Test
    public void shouldCreateMailMessageWithAllProperties() {
        mailMessage = MailMessage.builder().to(TO).subject(SUBJECT).text(TEXT).build();
        assertNotNull(mailMessage);
        assertEquals(mailMessage.getTo(), TO);
        assertEquals(mailMessage.getSubject(), SUBJECT);
        assertEquals(mailMessage.getText(), TEXT);
    }

    @Test
    public void shouldReplaceParametersInText() {
        String sampleText = "Some value: @key1@";
        String key = "@key1@";
        String value = "Placeholder";
        Map<String, String> sampleTextParameters = new HashMap<>();
        sampleTextParameters.put(key, value);
        mailMessage = MailMessage.builder().to(TO).subject(SUBJECT).text(sampleText).textParameters(sampleTextParameters).build();

        assertNotNull(mailMessage);
        assertTrue(mailMessage.getText().contains(value));
        assertFalse(mailMessage.getText().contains(key));
    }

    @Test
    public void shouldCreateMimeMessagePreparator() {
        mailMessage = MailMessage.builder().to(TO).subject(SUBJECT).text(TEXT).build();
        assertNotNull(mailMessage);
        MimeMessagePreparator mimeMessagePreparator = mailMessage.asMimeMessagePreparator();
        assertNotNull(mimeMessagePreparator);
    }

    @Test
    public void shouldCreateMailMessageFromTemplateFile() {
        String templateText = "This is my template";
        Path templateFilePath = Path.of("./template.html");

        assertDoesNotThrow(() -> Files.writeString(templateFilePath, templateText));
        assertDoesNotThrow(() ->  mailMessage = MailMessage.builder().to(TO).subject(SUBJECT).text(templateFilePath).build());
        assertNotNull(mailMessage);
        assertEquals(templateText, mailMessage.getText());
        assertDoesNotThrow(() ->  Files.delete(templateFilePath));
    }
}