package com.linkshorter.app.features.email.service;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.features.contactus.model.ContactFormDto;
import com.linkshorter.app.features.email.model.MailMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Disabled
class EmailServiceTest {

    private static final String TO = "mylinkshorternoreply@gmail.com";
    private static final String SUBJECT = "Some subject";
    private static final String TEXT = "Some text";
    @Autowired
    private EmailService emailService;
    private SimpleMailMessage simpleMailMessage;
    private MailMessage mailMessage;


    @BeforeEach
    void setUp() {
        simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(TO);
        simpleMailMessage.setSubject(SUBJECT);
        simpleMailMessage.setText(TEXT);
    }

    @AfterEach
    void tearDown() {
        simpleMailMessage = null;
    }

    @Test
    public void shouldSendSimpleMailMessage() {
        try {
            assertDoesNotThrow(() -> emailService.sendSimpleMessage(simpleMailMessage));
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldNotSendSimpleMailMessageWithNullAddress() {
        simpleMailMessage.setTo("");
        assertThrows(Exception.class, () -> emailService.sendSimpleMessage(simpleMailMessage));
    }

    @Test
    public void shouldSendMailMessage() {
        mailMessage = MailMessage.builder().to(TO).subject(SUBJECT).text(TEXT).build();
        assertDoesNotThrow(() -> emailService.send(mailMessage));
    }

    @Test
    public void shouldSendMailMessageWithHtmlContent() {
        String messageText = "<html><body><p>Hello this is <strong>sample test mail</strong></p></body></html>";
        Path temporaryHtmlTemplate = Path.of("./temporary-html-template.html");
        assertDoesNotThrow(() -> Files.writeString(temporaryHtmlTemplate, messageText));

        assertDoesNotThrow(() -> mailMessage = MailMessage.builder().to(TO).subject(SUBJECT).text(temporaryHtmlTemplate).build());
        assertDoesNotThrow(() -> emailService.send(mailMessage));
        assertDoesNotThrow(() -> Files.delete(temporaryHtmlTemplate));
    }

    @Test
    public void shouldReadHtmlTemplateToString() {
        String emailTemplateFilePath = "email-templates/account-confirmation.html";
        try {
            String emailTemplateFileContent = emailService.getEmailTemplateText(emailTemplateFilePath);

            assertNotNull(emailTemplateFileContent);
            assertTrue(emailTemplateFileContent.contains("naszym serwisie mylinkshorter.com"));
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldSendHtmlTemplateMessage() {
        String emailTemplateFilePath = "email-templates/account-confirmation.html";
        try {
            String emailTemplateFileContent = emailService.getEmailTemplateText(emailTemplateFilePath);
            mailMessage = MailMessage.builder().to(TO).subject(SUBJECT).text(emailTemplateFileContent).build();

            assertNotNull(emailTemplateFileContent);
            assertTrue(emailTemplateFileContent.contains("naszym serwisie mylinkshorter.com"));
            assertDoesNotThrow(() -> emailService.send(mailMessage));
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldThrowAnExceptionWhenTryingToReadNotExistingTemplate() {
        String nonExistingTemplateName = UUID.randomUUID().toString();
        assertThrows(Exception.class, () -> emailService.getEmailTemplateText(nonExistingTemplateName));
    }

    @Test
    public void shouldReadCustomEmailTemplateProperties() {
        Map<String, String> customProperties;
        try {
            customProperties = emailService.buildEmailTextParametersMap();

            assertNotNull(customProperties);
            assertEquals(2, customProperties.size());
            assertTrue(customProperties.containsKey("@application-link@"));
            assertTrue(customProperties.containsKey("@contact-form-email@"));
            System.out.println(customProperties);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldCreateEmailTemplatePropertiesForUser() {
        User user = new User("username", "password");
        user.setFullName("fullname");
        user.setEmail("some-email@gmail.com");

        try {
            Map<String, String> emailTemplateProperties = emailService.buildEmailTextParametersMap(user);

            assertNotNull(emailTemplateProperties);
            assertFalse(emailTemplateProperties.size() == 0);
            assertEquals(user.getFullName(), emailTemplateProperties.get("@user.fullname@"));
            assertEquals(user.getEmail(), emailTemplateProperties.get("@user.email@"));
            assertEquals(user.getId().toString(), emailTemplateProperties.get("@user.id@"));
            assertTrue(emailTemplateProperties.containsKey("@application-link@"));
        } catch (Exception exception) {
            assertNull(exception);
        }
    }

    @Test
    public void shouldSendEmailToUser() {
        User user = new User("username", "password");
        user.setFullName("Tomasz");
        user.setEmail(TO);
        String emailTemplateFilepath = "email-templates/account-confirmation.html";
        String emailSubject = "Aktywacja konta w serwisie mylinkshorter.com";
        assertDoesNotThrow(() -> emailService.sendEmailToUser(user, emailTemplateFilepath, emailTemplateFilepath));
    }

    @Test
    public void shouldSendEmailWithContactFormDto() {
        ContactFormDto contactFormDto = new ContactFormDto();
        contactFormDto.setEmail("some-mail@gmail.com");
        contactFormDto.setMessage("some message");
        contactFormDto.setName("Some name");
        String subject = "New message from contact form by " + contactFormDto.getEmail();
        String text = null;
        try {
            text = emailService.getEmailTemplateText("email-templates/contact-form-message.html");
        } catch (Exception e) {
            assertNull(e);
        }
        Map<String, String> textParameters = emailService.buildEmailTextParametersMap(contactFormDto);
        MailMessage mailMessage = MailMessage.builder().to(TO).subject(subject).textParameters(textParameters).text(text).build();
        emailService.send(mailMessage);
    }
}