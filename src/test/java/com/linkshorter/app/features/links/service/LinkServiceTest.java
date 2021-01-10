package com.linkshorter.app.features.links.service;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.repository.UserRepository;
import com.linkshorter.app.core.validator.ValidatorException;
import com.linkshorter.app.features.exception.UserExceedLinkLimitException;
import com.linkshorter.app.features.links.model.Link;
import com.linkshorter.app.features.links.model.LinkActivity;
import com.linkshorter.app.features.links.repository.LinkActivityRepository;
import com.linkshorter.app.features.links.repository.LinkRepository;
import com.linkshorter.app.features.links.setter.LinkSetter;
import com.linkshorter.app.features.links.validator.LinkValidator;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LinkServiceTest {


    private LinkService linkService;
    @Autowired
    private LinkRepository linkRepository;
    @Autowired
    private LinkActivityRepository linkActivityRepository;
    @Autowired
    private LinkValidator linkValidator;
    @Autowired
    private LinkSetter linkSetter;
    @Autowired
    private UserRepository userRepository;

    private User user;
    private Link link;

    private void setUpOnlyOnce() {
        if (this.linkService == null) {
            this.linkService = new LinkService(linkRepository, linkValidator,
                    linkSetter, linkActivityRepository);
        }
    }

    @BeforeEach
    void setUp() {
        setUpOnlyOnce();
        user = new User("user", "password");
        userRepository.save(user);

        linkActivityRepository.deleteAll();
        linkRepository.deleteAll();

        link = new Link();
        link.setLongLink("https://google.com");
        link.setCreationDate(java.sql.Date.valueOf(LocalDate.of(2020, 10, 10)));
        link.setExpirationDate(java.sql.Date.valueOf(LocalDate.now().plusDays(100)));
    }

    @AfterEach
    void tearDown() {
        linkActivityRepository.deleteAll();
        linkRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void shouldNotCreateLinkWIthNullLink() {
        Link link = null;
        assertThrows(Exception.class, () -> linkService.createLink(link));
    }

    @Test
    public void shouldCreateLinkWithNullUser() {
        link.setUser(null);
        assertDoesNotThrow(() -> linkService.createLink(link));
    }

    @Test
    public void shouldNotCreateLinkWithoutExpirationDate() {
        link.setExpirationDate(null);
        assertThrows(ValidatorException.class, () -> linkService.createLink(link));
    }

    @Test
    public void shouldNotCreateLinkWithExpirationDateEarlierThanCreationDate() {
        Date earlierExpirationDateThanCreationDate = java.sql.Date.valueOf(LocalDate.of(2020, 1, 1));
        link.setExpirationDate(earlierExpirationDateThanCreationDate);
        assertThrows(ValidatorException.class, () -> linkService.createLink(link));
    }

    @Test
    public void shouldNotCreateLinkWithoutExistingUser() {
        link.setUser(new User());
        assertThrows(Exception.class, () -> linkService.createLink(link));
    }

    @Test
    public void shouldCreateLinkWithGivenUser() {
        user.grantAuthority("registered-user");
        link.setUser(user);
        assertDoesNotThrow(() -> linkService.createLink(link));
    }

    @Test
    public void shouldNotCreateLinkWithNotUniqueShortLink() {
        String customShortLink = "TEST";
        Link otherLink = new Link();
        otherLink.setCreationDate(link.getCreationDate());
        otherLink.setExpirationDate(link.getExpirationDate());
        otherLink.setLongLink(link.getLongLink());
        otherLink.setShortLink(customShortLink);
        link.setShortLink(customShortLink);

        assertDoesNotThrow(() -> linkService.createLink(link));
        assertThrows(Exception.class, () -> linkService.createLink(otherLink));
    }

    @Test
    public void shouldCreateUniqueShortLink() {
        String uniqueShortLink = linkService.createUniqueShortLink();
        Link otherLink = new Link();
        otherLink.setCreationDate(link.getCreationDate());
        otherLink.setExpirationDate(link.getExpirationDate());
        otherLink.setLongLink(link.getLongLink());
        otherLink.setShortLink(uniqueShortLink);

        assertDoesNotThrow(() -> linkService.createLink(link));
        assertDoesNotThrow(() -> linkService.createLink(otherLink));
    }

    @Test
    public void shouldCreateLinkBelow400ms() {
        long startTime = System.currentTimeMillis();
        assertDoesNotThrow(() -> linkService.createLink(link));
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        assertTrue(totalTime < 400);
    }

    @Test
    public void shouldNotDeleteLinkIfIsNullable() {
        Link someLink = null;

        assertThrows(Exception.class, () -> linkService.deleteLink(someLink, user));
    }

    @Test
    public void shouldNotDeleteLinkIfUserIsNull() {
        User someUser = null;

        assertThrows(Exception.class, () -> linkService.deleteLink(link, someUser));
    }

    @Test
    public void shouldThrowsAnErrorIfUserAssignedToLinkIsDifferentThanGiven() {
        User otherUser = new User("otheruser", "password");
        userRepository.save(otherUser);
        link.setUser(user);

        assertThrows(Exception.class, () -> linkService.deleteLink(link, otherUser));
    }

    @Test
    public void shouldDeleteLinkIfLinkIsAssignedToUser() {
        link.setUser(user);
        linkRepository.save(link);

        assertDoesNotThrow(() -> linkService.deleteLink(link, user));
    }

    @Test
    @Transactional
    public void shouldDeleteOnlyOneLinkWithGivenId() {
        link.setUser(user);
        linkRepository.save(link);

        assertDoesNotThrow(() -> linkService.deleteLink(link, user));
        boolean notExistDeletedLink = linkRepository.findById(link.getId()).isEmpty();
        assertTrue(notExistDeletedLink);
    }

    @Test
    @Transactional
    public void shouldDeleteOnlyOneLink() {
        LinkActivity linkActivity = new LinkActivity();
        linkActivity.setLink(link);
        linkActivity.setActivityDate(new Date());
        link.setUser(user);
        linkRepository.save(link);
        linkActivityRepository.save(linkActivity);

        assertDoesNotThrow(() -> linkService.deleteLink(link, user));
        List<Link> links = linkRepository.findAll();
        List<LinkActivity> deletedLinkLinkActivities = linkActivityRepository.findLinkActivitiesByLink(link);
        assertEquals(0, links.size());
        assertEquals(0, deletedLinkLinkActivities.size());
        assertFalse(linkRepository.existsById(link.getId()));
    }

    @Test
    @Transactional
    public void shouldRemoveLinkAndAllLinkActivityEntities() {
        link.setUser(user);
        linkRepository.save(link);
        linkActivityRepository.save(new LinkActivity(link, new Date()));
        linkActivityRepository.save(new LinkActivity(link, new Date()));
        link.getLinkActivities().addAll(linkActivityRepository.findAll());
        linkRepository.save(link);

        assertDoesNotThrow(() -> linkService.deleteLink(link, user));
        assertEquals(0, linkActivityRepository.count());
        assertEquals(0, linkRepository.findAllByUser(user).size());
    }

    @Test
    public void shouldFindAllLinksRelatedToUser() {
        link.setUser(user);
        linkRepository.save(link);

        assertEquals(1, linkService.findAllByUser(user).size());
        assertEquals(true, linkService.findByIdAndUser(link.getId(), user).isPresent());
    }

    @Test
    public void shouldFindByShortLinkAndActiveIsTrueAAndExpirationDateBeforeNow() {
        link.setUser(user);
        link.setActive(true);

        LocalDate now = LocalDate.now();
        Date expirationDate = java.sql.Date.valueOf(now.plusDays(2));
        link.setExpirationDate(expirationDate);
        linkRepository.save(link);

        String shortLink = link.getShortLink();
        Optional<Link> foundLink = linkService.findByShortLinkAndActiveIsTrueAAndExpirationDateBeforeNow(shortLink);
        assertTrue(foundLink.isPresent());
    }

    @Test
    public void shouldNotFindByShortLinkIfIsInactive() {
        link.setActive(false);
        linkRepository.save(link);

        String shortLink = link.getShortLink();
        Optional<Link> foundLink = linkService.findByShortLinkAndActiveIsTrueAAndExpirationDateBeforeNow(shortLink);

        assertTrue(foundLink.isEmpty());
    }

    @Test
    public void shouldNotFindBYShortLinkIfExpirationDateIsAfterCurrentDate() {
        link.setUser(user);
        link.setActive(true);
        LocalDate now = LocalDate.now();
        Date expirationDate = java.sql.Date.valueOf(now.minusDays(2));
        link.setExpirationDate(expirationDate);

        linkRepository.save(link);
        String shortLink = link.getShortLink();
        Optional<Link> foundLink = linkService.findByShortLinkAndActiveIsTrueAAndExpirationDateBeforeNow(shortLink);
        assertTrue(foundLink.isEmpty());
    }

    @Test
    public void shouldShowAccessToUserWithConnectionToLink() {
        link.setUser(user);
        linkRepository.save(link);

        UUID linkId = link.getId();
        boolean hasAccessToLink = linkService.hasUserAccessToLink(linkId, user);

        assertTrue(hasAccessToLink);
    }

    @Test
    public void shouldNotShowAccessToUserWithNoConnectionToLink() {
        link.setUser(user);
        linkRepository.save(link);

        User otherUser = new User("otheruser", "password");
        userRepository.save(otherUser);
        UUID linkId = link.getId();
        boolean hasAccessToLink = linkService.hasUserAccessToLink(linkId, otherUser);

        assertFalse(hasAccessToLink);
    }

    @Test
    public void shouldThrowExceptionWhenAnyArgumentForUpdateIsNull() {
        assertThrows(Exception.class, () -> linkService.updateLink(null, null, null));
    }

    @Test
    public void shouldNotUpdateLinkIfUserDoesntHavePermissionsToLink() {
        link.setUser(user);
        linkRepository.save(link);
        User otherUser = new User("otheruser", "password");

        assertThrows(Exception.class, () -> linkService.updateLink(link.getId(), link, otherUser));
    }

    @Test
    public void shouldNotUpdateLinkId() {
        UUID linkId = link.getId();
        UUID randomId = UUID.randomUUID();
        link.setUser(user);
        link.setShortLink("TEST123");
        link.setLongLink("https://google.com");
        linkRepository.save(link);

        try {
            link.setId(randomId);
            Link updatedLink = linkService.updateLink(linkId, link, user);
            assertNotEquals(updatedLink.getId(), randomId);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldUpdateAllLinkFields() {
        Link linkWithNewData = new Link();
        LocalDate now = LocalDate.now();
        Date expirationDate = java.sql.Date.valueOf(now.plusDays(2));
        linkWithNewData.setExpirationDate(expirationDate);
        linkWithNewData.setActive(true);
        linkWithNewData.setLongLink("https://google.com");
        linkWithNewData.setShortLink("TESTGOOGLE");
        link.setCreationDate(java.sql.Date.valueOf(now.minusDays(2)));
        link.setUser(user);
        linkRepository.save(link);

        try {
            Link updatedLink = linkService.updateLink(link.getId(), linkWithNewData, user);
            assertEquals(linkWithNewData.getExpirationDate(), updatedLink.getExpirationDate());
            assertEquals(linkWithNewData.isActive(), updatedLink.isActive());
            assertEquals(linkWithNewData.getLongLink(), updatedLink.getLongLink());
            assertEquals(linkWithNewData.getShortLink(), updatedLink.getShortLink());
        } catch (Exception exception) {
            assertNull(exception);
        }
    }

    @Test
    public void shouldThrowErrorIfCheckingPermissionsForUserNull() {
        link.setUser(null);
        linkRepository.save(link);
        assertThrows(Exception.class, () -> linkService.hasUserAccessToLink(link.getId(), null));
    }

    @Test
    public void shouldThrowExceptionIfCheckingPermissionsForNullableId() {
        assertThrows(Exception.class, () -> linkService.hasUserAccessToLink(null, user));
    }

    @Test
    public void shouldDeleteLinkAndPersistUser() {
        link.setUser(user);
        userRepository.save(user);
        linkRepository.save(link);

        assertDoesNotThrow(() -> linkService.deleteLink(link, user));
        User userFetchedFromDatabase = userRepository.findByUsername(user.getUsername()).get();
        assertNotNull(userFetchedFromDatabase);
        assertEquals(user.getUsername(), userFetchedFromDatabase.getUsername());
    }

    @Test
    public void shouldNotAllowToCreateLinksAboveUserLimit() {
        User registeredUser = new User("some-usr-email@some-domain.com", "some-password");
        registeredUser.grantAuthority("registered-user");
        userRepository.save(registeredUser);

        long counter = 0;
        while(counter < 20) {
            Link otherLink = new Link(link);
            otherLink.setUser(registeredUser);
            otherLink.setShortLink(RandomStringUtils.randomAlphanumeric(10));
            try {
                assertDoesNotThrow(() -> linkService.createLink(otherLink));
            } catch (Exception e) {
                assertNull(e);
            }
            counter++;
        }
        assertEquals(20, linkRepository.countLinksByUser(registeredUser));
        Link otherLink = new Link(link);
        otherLink.setUser(registeredUser);
        otherLink.setShortLink(RandomStringUtils.randomAlphanumeric(10));
        assertThrows(UserExceedLinkLimitException.class, () -> linkService.createLink(otherLink));
    }
}