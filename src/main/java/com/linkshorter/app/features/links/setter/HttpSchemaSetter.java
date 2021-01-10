package com.linkshorter.app.features.links.setter;

import com.linkshorter.app.core.setter.Setter;
import com.linkshorter.app.features.links.model.Link;
import com.linkshorter.app.features.links.validator.URLValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class HttpSchemaSetter implements Setter<Link> {

    private static final Logger log = LoggerFactory.getLogger(HttpSchemaSetter.class);
    private final URLValidator urlValidator;

    public HttpSchemaSetter(URLValidator urlValidator) {
        this.urlValidator = urlValidator;
    }

    @Override
    public void set(Link link) {

        if (link != null) {
            String longLink = link.getLongLink();
            boolean isLongLinkValid = urlValidator.isValid(longLink);
            if (!isLongLinkValid) {
                tryToSetLinkWithAddedHttpSchema(link);
            }
        }
    }

    private void tryToSetLinkWithAddedHttpSchema(Link link) {
        String longLink = link.getLongLink();
        log.debug("Link isn't valid. Trying to add http schema");
        String longLinkWithAddedHttpSchema = String.format("http://%s", longLink);
        boolean isLongLinkWithAddedHttpSchemaValid = urlValidator.isValid(longLinkWithAddedHttpSchema);
        if (isLongLinkWithAddedHttpSchemaValid) {
            log.debug("Link with added http schema is valid. Changing old long link: {} to new long link: {}", longLink, longLinkWithAddedHttpSchema);
            link.setLongLink(longLinkWithAddedHttpSchema);
        }
    }
}
