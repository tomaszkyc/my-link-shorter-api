package com.linkshorter.app.features.links.repository;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.features.links.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LinkRepository extends JpaRepository<Link, UUID> {
    List<Link> findAllByUser(User user);

    Optional<Link> findByIdAndUser(UUID id, User user);

    Optional<Link> findByShortLinkAndActiveIsTrueAndExpirationDateAfter(String shortLink, Date now);

    boolean existsLinkByIdAndUser(UUID id, User user);

    boolean existsByShortLink(String shortLink);

    long countLinksByUser(User user);
}
