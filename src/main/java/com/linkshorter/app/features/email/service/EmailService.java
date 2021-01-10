package com.linkshorter.app.features.email.service;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.features.email.model.MailMessage;
import com.linkshorter.app.features.email.service.parameters.factory.TextParametersFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class EmailService {

    @Value("${app.mail.from-mail-address}")
    private String fromAddress;
    @Value("${app.mail.from-mail-displayname}")
    private String fromDisplayName;

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender emailSender;
    private final TextParametersFactory textParametersFactory;

    public EmailService(JavaMailSender emailSender, TextParametersFactory textParametersFactory) {
        this.emailSender = emailSender;
        this.textParametersFactory = textParametersFactory;
    }

    public void sendSimpleMessage(SimpleMailMessage simpleMailMessage) {
        emailSender.send(simpleMailMessage);
    }

    public void send(MailMessage mailMessage) {
        log.trace("Zostanie wysłana wiadomość: {}", mailMessage.toString());
        setFromProperties(mailMessage);
        MimeMessagePreparator mimeMessagePreparator = mailMessage.asMimeMessagePreparator();
        log.trace("Próbuje wysłać wiadomość");
        emailSender.send(mimeMessagePreparator);
        log.debug("Wiadomość została wysłana");
    }

    public String getEmailTemplateText(String emailTemplateFilePath) throws Exception {
        if (StringUtils.isBlank(emailTemplateFilePath)) {
            throw new Exception("Given filepath is null or empty");
        }
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(emailTemplateFilePath);
        if (inputStream == null) {
            throw new Exception("File in location: " + emailTemplateFilePath + " not found");
        }
        return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.joining(System.lineSeparator()));
    }

    public Map<String, String> buildEmailTextParametersMap(Object object) {
        return this.textParametersFactory.createTextParametersMap(object);
    }

    public Map<String, String> buildEmailTextParametersMap() {
        return this.textParametersFactory.createTextParametersMap(null);
    }

    public void sendEmailToUser(User user, String subject, String emailTemplateFilepath) throws Exception {
        Objects.requireNonNull(user, "Given user object is null");
        log.debug("Rozpoczynam wysyłanie maila do użytkownika: {} z szablonu: {}", user, emailTemplateFilepath);
        String emailText = this.getEmailTemplateText(emailTemplateFilepath);
        String to = user.getEmail();
        Map<String, String> textParameters = this.buildEmailTextParametersMap(user);
        log.trace("Zbudowana mapa parametrów dla użytkownika: {}", textParameters);
        MailMessage mailMessage = MailMessage.builder().to(to).text(emailText).subject(subject).textParameters(textParameters).build();
        this.send(mailMessage);
    }

    private void setFromProperties(MailMessage mailMessage) {
        log.trace("Rozpoczynam ustawianie nagłówków nadawcy wiadomości email");
        mailMessage.setFromAddress(fromAddress);
        mailMessage.setFromDisplayName(fromDisplayName);
        log.trace("Zakończono ustawianie nagłówków nadawcy wiadomości email");
    }
}
