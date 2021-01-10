package com.linkshorter.app.features.links.task;

import com.linkshorter.app.features.links.model.Link;
import com.linkshorter.app.features.links.model.LinkActivity;
import com.linkshorter.app.features.links.repository.LinkActivityRepository;
import com.linkshorter.app.features.links.service.LinkActivityService;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class LinkAnalyzeTask implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(LinkAnalyzeTask.class);
    private UserAgentAnalyzer userAgentAnalyzer;
    private final LinkActivityRepository linkActivityRepository;
    private String userAgentHeader;
    private Link link;
    private Date activityDate;

    public LinkAnalyzeTask(LinkActivityRepository linkActivityRepository) {
        this.linkActivityRepository = linkActivityRepository;
        userAgentAnalyzer =  UserAgentAnalyzer.newBuilder()
                                        .hideMatcherLoadStats()
                                        .build();
    }

    public LinkActivity parseUserAgentHeader(String userAgentHeader) {
        long startTime = System.currentTimeMillis();
        LinkActivity linkActivity = new LinkActivity();
        UserAgent userAgent = userAgentAnalyzer.parse(userAgentHeader);
        mapUserAgentFieldsToLinkActivityFields(userAgent, linkActivity);
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        log.info("Analying user agent header took: {} ms", executionTime);
        return linkActivity;
    }

    private void mapUserAgentFieldsToLinkActivityFields(UserAgent userAgent, LinkActivity linkActivity) {
        linkActivity.setDeviceClass(userAgent.getValue(UserAgent.DEVICE_CLASS));
        linkActivity.setDeviceName(userAgent.getValue(UserAgent.DEVICE_NAME));
        linkActivity.setDeviceBrand(userAgent.getValue(UserAgent.DEVICE_BRAND));
        linkActivity.setOsClass(userAgent.getValue(UserAgent.OPERATING_SYSTEM_CLASS));
        linkActivity.setOsName(userAgent.getValue(UserAgent.OPERATING_SYSTEM_NAME));
        linkActivity.setOsVersion(userAgent.getValue(UserAgent.OPERATING_SYSTEM_VERSION));
        linkActivity.setAgentClass(userAgent.getValue(UserAgent.AGENT_CLASS));
        linkActivity.setAgentName(userAgent.getValue(UserAgent.AGENT_NAME));
        linkActivity.setAgentVersion(userAgent.getValue(UserAgent.AGENT_VERSION));
        linkActivity.setAgentVersionMajor(userAgent.getValue(UserAgent.AGENT_VERSION_MAJOR));
    }

    public String getUserAgentHeader() {
        return userAgentHeader;
    }

    public void setUserAgentHeader(String userAgentHeader) {
        this.userAgentHeader = userAgentHeader;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public Date getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(Date activityDate) {
        this.activityDate = activityDate;
    }

    @Override
    public void run() {
        log.debug("Started analyzing header: {}", this.userAgentHeader);
        LinkActivity linkActivity = this.parseUserAgentHeader(this.userAgentHeader);
        log.debug("Finished analyzing header: {}", this.userAgentHeader);
        linkActivity.setLink(link);
        linkActivity.setActivityDate(activityDate);
        linkActivityRepository.save(linkActivity);
        log.debug("activity saved in database");
    }
}
