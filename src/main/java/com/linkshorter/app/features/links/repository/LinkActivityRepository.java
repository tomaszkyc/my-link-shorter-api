package com.linkshorter.app.features.links.repository;

import com.linkshorter.app.features.links.model.Link;
import com.linkshorter.app.features.links.model.LinkActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Repository
public interface LinkActivityRepository extends JpaRepository<LinkActivity, UUID> {

    List<LinkActivity> findLinkActivitiesByLink(Link link);

    List<LinkActivity> findAllByLinkIsIn(List<Link> links);

    List<LinkActivity> findAllByActivityDateIsBetweenAndLinkIsIn(Date dateFrom, Date dateTo, List<Link> links);
}
