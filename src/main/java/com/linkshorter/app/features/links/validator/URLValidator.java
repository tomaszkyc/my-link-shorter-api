package com.linkshorter.app.features.links.validator;

import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.stereotype.Component;

@Component
public class URLValidator {

    private final UrlValidator urlValidator;

    public URLValidator() {
        this.urlValidator = new UrlValidator(UrlValidator.ALLOW_ALL_SCHEMES);
    }

    public boolean isValid(String url) {
        return this.urlValidator.isValid(url);
    }
}
