package com.linkshorter.app.features.contactus.service;

import com.linkshorter.app.features.contactus.model.ContactFormDto;
import com.linkshorter.app.features.email.model.MailMessage;
import com.linkshorter.app.features.email.service.EmailService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class ContactFormDtoService {

    private static final Logger log = LoggerFactory.getLogger(ContactFormDtoService.class);

    private final EmailService emailService;

    public ContactFormDtoService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void validate(ContactFormDto contactFormDto) throws Exception {
        validateContactFormDto(contactFormDto);
    }

    private void validateContactFormDto(ContactFormDto contactFormDto) throws Exception {
        Objects.requireNonNull(contactFormDto, "Podany obiekt jest null");
        Objects.requireNonNull(contactFormDto.getCreationDate(), "Data stworzenia formularza nie może być pusta");
        String email = contactFormDto.getEmail();
        String name = contactFormDto.getName();
        String message = contactFormDto.getMessage();

        if (StringUtils.isBlank(email)) {
            throw new Exception("Adres email jest wymagany");
        }
        if (StringUtils.isBlank(name)) {
            throw new Exception("Nazwa jest wymagana");
        }
        if (StringUtils.isBlank(message)) {
            throw new Exception("Wiadomość jest wynagana");
        }
    }

    public void sendContactFormDtoByMail(ContactFormDto contactFormDto) throws Exception {
        this.validate(contactFormDto);
        String contactFormCreatedDate = contactFormDto.getCreationDateAsLocalDateTime().toString();
        String contactFormEmail = contactFormDto.getEmail();
        String mailSubject = String.format("Nowa wiadomość z formularza kontaktowego od %s z dnia %s", contactFormEmail, contactFormCreatedDate);
        String emailTemplateFilepath = "email-templates/contact-form-message.html";
        String text = emailService.getEmailTemplateText(emailTemplateFilepath);
        Map<String, String> textParameters = emailService.buildEmailTextParametersMap(contactFormDto);
        String to = textParameters.get("@contact-form-email@");
        MailMessage mailMessage = MailMessage.builder().to(to).subject(mailSubject).text(text).textParameters(textParameters).build();
        this.emailService.send(mailMessage);
    }
}
