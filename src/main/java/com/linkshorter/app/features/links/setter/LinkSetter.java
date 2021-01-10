package com.linkshorter.app.features.links.setter;

import com.linkshorter.app.core.setter.Setter;
import com.linkshorter.app.features.links.model.Link;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class LinkSetter implements Setter<Link> {

    private final Set<Setter<Link>> linkSetters;

    public LinkSetter(Set<Setter<Link>> linkSetters) {
        this.linkSetters = linkSetters;
    }

    @Override
    public void set(Link link) {
        linkSetters.forEach(s -> s.set(link));
    }
}
