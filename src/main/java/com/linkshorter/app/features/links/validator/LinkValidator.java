package com.linkshorter.app.features.links.validator;

import com.linkshorter.app.core.validator.Validator;
import com.linkshorter.app.core.validator.ValidatorException;
import com.linkshorter.app.features.links.model.Link;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.regex.Pattern;

@Component
public class LinkValidator implements Validator<Link> {

    private final static Pattern WHITESPACE_PATTERN = Pattern.compile("\\s");
    private final URLValidator urlValidator;

    public LinkValidator(URLValidator urlValidator) {
        this.urlValidator = urlValidator;
    }

    @Override
    public void validate(Link link) throws ValidatorException {

        if (link == null) {
            throw new ValidatorException("Podany link jest pusty");
        }

        if (StringUtils.isBlank(link.getShortLink())) {
            throw new ValidatorException("Krótki link nie może być pusty");
        }

        if (StringUtils.isBlank(link.getLongLink())) {
            throw new ValidatorException("Długi link nie może być pusty");
        }

        Date creationDate = link.getCreationDate();
        if (creationDate == null) {
            throw new ValidatorException("Data utworzenia nie może być pusty");
        }
        Date expirationDate = link.getExpirationDate();
        if (expirationDate == null) {
            throw new ValidatorException("Data wygaśnięcia nie może być pusty");
        }

        if (expirationDate.before(creationDate)) {
            throw new ValidatorException("Data wygaśnięcia linku nie może być wcześniejsza niż data utworzenia linku");
        }

        validateWhiteSpaces(link);
        validateLinkURLs(link);

    }

    private void validateWhiteSpaces(Link link) throws ValidatorException {
        String shortLink = link.getShortLink();
        String longLink = link.getLongLink();
        if (WHITESPACE_PATTERN.matcher(shortLink).find()) {
            throw new ValidatorException("Krótki link nie może zawierać znaków białych (spacje, tabulatory)");
        }
        if (WHITESPACE_PATTERN.matcher(longLink).find()) {
            throw new ValidatorException("Długi link nie może zawierać znaków białych (spacje, tabulatory)");
        }
    }

    private void validateLinkURLs(Link link) throws ValidatorException {
        String longLink = link.getLongLink();
        boolean isValidURLFromLongLink = this.urlValidator.isValid(longLink);
        if (!isValidURLFromLongLink) {
            throw new ValidatorException("Podany długi link nie jest poprawny. Wprowadź poprawny link i spróbuj ponownie");
        }
    }
}
