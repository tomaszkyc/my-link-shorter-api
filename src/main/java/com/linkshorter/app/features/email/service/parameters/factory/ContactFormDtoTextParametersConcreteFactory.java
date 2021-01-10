package com.linkshorter.app.features.email.service.parameters.factory;

import com.linkshorter.app.features.contactus.model.ContactFormDto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ContactFormDtoTextParametersConcreteFactory implements TextParametersAbstractFactory {

    @Override
    public Map<String, String> createTextParametersMap(Object object) {
        Map<String, String> parameters = new HashMap<>();
        Objects.requireNonNull(object, "Given object cannot be null");
        ContactFormDto contactFormDto = (ContactFormDto)object;
        buildTextParameters(contactFormDto, parameters);
        return parameters;
    }

    private void buildTextParameters(ContactFormDto contactFormDto, Map<String, String> parameters) {
        LocalDateTime creationDate = LocalDateTime.ofInstant(contactFormDto.getCreationDate().toInstant(), ZoneId.systemDefault());
        parameters.put("@contact-form-dto.creation-date@", creationDate.toString());
        parameters.put("@contact-form-dto.contact-mail@", contactFormDto.getEmail());
        parameters.put("@contact-form-dto.name@", contactFormDto.getName());

        String plainMessage = contactFormDto.getMessage();
        String properFormattedMessage = replaceLineSeparatorsWithHtmlBrTag(plainMessage);
        parameters.put("@contact-form-dto.message@", properFormattedMessage);
    }

    private String replaceLineSeparatorsWithHtmlBrTag(String plainMessage) {
        String replacedMessage = new String(plainMessage.replaceAll("\n", "<br>"));
        return replacedMessage;
    }
}
