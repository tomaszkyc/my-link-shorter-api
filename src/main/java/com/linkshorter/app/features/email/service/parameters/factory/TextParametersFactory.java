package com.linkshorter.app.features.email.service.parameters.factory;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.features.contactus.model.ContactFormDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class TextParametersFactory {

    @Value("#{${app.mail.custom-email-template-parameters}}")
    private Map<String, String> customEmailTemplateParameters;

    public Map<String, String> createTextParametersMap(Object object) {
        Map<String, String> textParameters = new HashMap<>(customEmailTemplateParameters);
        TextParametersAbstractFactory textParametersAbstractFactory;
        if (object instanceof User) {
            textParametersAbstractFactory = new UserTextParametersConcreteFactory();
            Map<String, String> additionalParameters = textParametersAbstractFactory.createTextParametersMap(object);
            textParameters.putAll(additionalParameters);
        } else if (object instanceof ContactFormDto) {
            textParametersAbstractFactory = new ContactFormDtoTextParametersConcreteFactory();
            Map<String, String> additionalParameters = textParametersAbstractFactory.createTextParametersMap(object);
            textParameters.putAll(additionalParameters);
        }
        return textParameters;
    }

}
