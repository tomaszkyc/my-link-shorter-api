package com.linkshorter.app.features.links.service;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.features.links.model.Link;
import com.linkshorter.app.features.links.model.LinkActivity;
import com.linkshorter.app.features.links.repository.LinkActivityRepository;
import com.linkshorter.app.features.links.task.LinkAnalyzeTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class LinkActivityService {

    private static final Logger log = LoggerFactory.getLogger(LinkActivityService.class);
    private final LinkActivityRepository linkActivityRepository;
    private final LinkService linkService;

    public LinkActivityService(LinkActivityRepository linkActivityRepository,
                               LinkService linkService) {
        this.linkActivityRepository = linkActivityRepository;
        this.linkService = linkService;
    }

    public List<LinkActivity> findAllByLinkIdAndUser(UUID linkId, User user) throws Exception {
        boolean hasUserAccessToLink = linkService.hasUserAccessToLink(linkId, user);
        if (!hasUserAccessToLink) {
            throw new Exception("Nie masz uprawnień do pobrania aktywności linku o id: " + linkId);
        }

        Link link = linkService.findByIdAndUser(linkId, user).get();
        return linkActivityRepository.findLinkActivitiesByLink(link);
    }

    public void processUserAgentHeaderAsync(String userAgentHeader, Link link, Date activityDate) {
        LinkAnalyzeTask linkAnalyzeTask = new LinkAnalyzeTask(linkActivityRepository);
        linkAnalyzeTask.setUserAgentHeader(userAgentHeader);
        linkAnalyzeTask.setLink(link);
        linkAnalyzeTask.setActivityDate(activityDate);
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(linkAnalyzeTask);
    }

}
