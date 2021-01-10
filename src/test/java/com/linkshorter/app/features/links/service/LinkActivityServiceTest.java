package com.linkshorter.app.features.links.service;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.repository.UserRepository;
import com.linkshorter.app.features.links.model.Link;
import com.linkshorter.app.features.links.model.LinkActivity;
import com.linkshorter.app.features.links.repository.LinkActivityRepository;
import com.linkshorter.app.features.links.repository.LinkRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class LinkActivityServiceTest {

    private static final String SAMPLE_AGENT_HEADER = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.130 Safari/537.36";

    @Autowired
    private LinkActivityRepository linkActivityRepository;
    @Autowired
    private LinkService linkService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LinkRepository linkRepository;
    private LinkActivityService linkActivityService;
    private User user;
    private Link link;


    @BeforeEach
    public void init() {
        setupOnlyOnce();

        user = new User("user", "password");
        userRepository.save(user);

        linkActivityRepository.deleteAll();
        linkRepository.deleteAll();

        link = new Link();
        link.setLongLink("https://google.com");
        link.setCreationDate(java.sql.Date.valueOf(LocalDate.of(2020, 10, 14)));
        link.setExpirationDate(java.sql.Date.valueOf(LocalDate.of(2020, 10, 15)));

    }

    @AfterEach
    public void tearDown() {
        linkActivityRepository.deleteAll();
        linkRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void setupOnlyOnce() {
        if (linkActivityService == null) {
            linkActivityService = new LinkActivityService(linkActivityRepository,
                    linkService);
        }
    }

    @Test
    public void shouldProcessAsyncRequestAndSaveInRepository() {
        link.setUser(user);
        linkRepository.save(link);
        Date activityDate = new Date();
        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> linkActivityService.processUserAgentHeaderAsync(SAMPLE_AGENT_HEADER, link, activityDate));
        boolean isLinkActivityProcessed = false;

        while (!isLinkActivityProcessed) {
            try {
                isLinkActivityProcessed = linkActivityService.findAllByLinkIdAndUser(link.getId(), user).size() > 0;
                Thread.sleep(1000L);
            } catch (Exception e) {
                assertNull(e);
            }

        }
        long totalTime = System.currentTimeMillis() - startTime;
        System.out.println("Totaltime: " + totalTime);
        assertTrue(totalTime < 10000L);
    }

    @Test
    public void shouldNotFindLinkActivityIfUserDontHavePermissionsToLink() {
        link.setUser(user);
        linkRepository.save(link);
        User otherUser = new User("otheruser", "password");

        assertThrows(Exception.class, () -> linkActivityService.findAllByLinkIdAndUser(link.getId(), otherUser));
    }

    @Test
    public void shouldFindLinkActivityIfUserHasPermissionsToLink() {
        link.setUser(user);
        linkRepository.save(link);

        assertDoesNotThrow(() -> linkActivityService.findAllByLinkIdAndUser(link.getId(), user));
    }

    @Test
    public void shouldFindAllLinkActivitiesIfUserHasPermissionToLink() {
        link.setUser(user);
        linkRepository.save(link);
        List<LinkActivity> linkActivities = new ArrayList<>();
        linkActivities.add(new LinkActivity(link, new Date()));
        linkActivities.add(new LinkActivity(link, new Date()));

        assertDoesNotThrow(() -> linkActivityRepository.saveAll(linkActivities));
        List<LinkActivity> linkActivitiesFetchFromRepository = null;
        try {
            linkActivitiesFetchFromRepository = linkActivityService.findAllByLinkIdAndUser(link.getId(), user);
        } catch (Exception e) {
            assertNull(e);
        }
        assertNotNull(linkActivitiesFetchFromRepository);
        assertEquals(2, linkActivitiesFetchFromRepository.size());
    }

}