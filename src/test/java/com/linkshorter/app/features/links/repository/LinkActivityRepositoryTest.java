package com.linkshorter.app.features.links.repository;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.repository.UserRepository;
import com.linkshorter.app.features.links.model.Link;
import com.linkshorter.app.features.links.model.LinkActivity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LinkActivityRepositoryTest {

    @Autowired
    private LinkActivityRepository linkActivityRepository;

    @Autowired
    private LinkRepository linkRepository;

    @Autowired
    private UserRepository userRepository;

    private Link link;
    private User user;

    @BeforeEach
    void setUp() {
        createTestUser();
        createTestLink();
        boolean linkExists = linkRepository.existsByShortLink(link.getShortLink());
        if (!linkExists) {
            linkRepository.save(link);
        }
        boolean userExists = userRepository.existsUserByUsername(user.getUsername());
        if (!userExists) {
            userRepository.save(user);
        }

    }

    @AfterEach
    void tearDown() {
        linkActivityRepository.deleteAll();
        linkRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void createTestLink() {
        this.link = new Link();
        link.setShortLink("TEST");
        link.setLongLink("https://google.com");
        link.setCreationDate(new Date());
        link.setExpirationDate(new Date());
        link.setActive(true);
    }

    private void createTestUser() {
        this.user = new User("user@linkshorter.com", "password");
    }

    @Test
    public void shouldSaveLinkActivityInRepository() {
        LinkActivity linkActivity = new LinkActivity();
        linkActivity.setLink(link);
        linkActivity.setActivityDate(new Date());
        assertDoesNotThrow(() -> linkActivityRepository.save(linkActivity));
        assertEquals(1L, linkActivityRepository.count());
    }

    @Test
    public void shouldFindActivitiesByLinks() {
        LocalDate now = LocalDate.now();
        Date today = java.sql.Date.valueOf(now);
        LinkActivity linkActivity = new LinkActivity();
        linkActivity.setLink(link);
        linkActivity.setActivityDate(today);
        LinkActivity otherLinkActivity = new LinkActivity();
        otherLinkActivity.setLink(link);
        linkActivity.setActivityDate(today);
        linkActivityRepository.saveAll(Arrays.asList(linkActivity, otherLinkActivity));

        List<LinkActivity> linkActivities = linkActivityRepository.findAllByLinkIsIn(Arrays.asList(link));
        assertNotNull(linkActivities);
        assertEquals(2, linkActivities.size());
    }

    @Test
    public void shouldFindActivitiesByLinksAndActivityDateBetween() {
        LocalDate now = LocalDate.now();
        Date yesterday = java.sql.Date.valueOf(now.minusDays(1));
        Date tomorrow = java.sql.Date.valueOf(now.plusDays(1));
        Date today = new Date();
        LinkActivity linkActivity = new LinkActivity();
        linkActivity.setLink(link);
        linkActivity.setActivityDate(today);

        LinkActivity otherLinkActivity = new LinkActivity();
        otherLinkActivity.setLink(link);
        otherLinkActivity.setActivityDate(today);

        linkActivityRepository.saveAll(Arrays.asList(linkActivity, otherLinkActivity));

        List<LinkActivity> linkActivities = linkActivityRepository.findAllByActivityDateIsBetweenAndLinkIsIn(yesterday, tomorrow, Arrays.asList(link));
        assertNotNull(linkActivities);
        assertEquals(2, linkActivities.size());
    }
}