package com.linkshorter.app.features.links.setter;

import com.linkshorter.app.core.setter.Setter;
import com.linkshorter.app.features.links.model.Link;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class LinkCreationDateSetter implements Setter<Link> {

    @Override
    public void set(Link link) {
        if (link != null) {
            link.setCreationDate(new Date());
        }
    }
}
