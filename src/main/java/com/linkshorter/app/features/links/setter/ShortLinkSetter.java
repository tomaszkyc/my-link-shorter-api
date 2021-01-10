package com.linkshorter.app.features.links.setter;

import com.linkshorter.app.core.setter.Setter;
import com.linkshorter.app.features.links.model.Link;
import com.linkshorter.app.features.links.service.LinkService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class ShortLinkSetter implements Setter<Link> {

    private final LinkService linkService;

    public ShortLinkSetter(@Lazy LinkService linkService) {
        this.linkService = linkService;
    }

    @Override
    public void set(Link link) {
        String shortLink = link.getShortLink();
        boolean isShortLinkEmpty = StringUtils.isBlank(shortLink);
        if (isShortLinkEmpty) {
            String newUniqueShortLink = linkService.createUniqueShortLink();
            link.setShortLink(newUniqueShortLink);
        }
    }
}
