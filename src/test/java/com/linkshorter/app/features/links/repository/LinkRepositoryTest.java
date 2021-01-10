package com.linkshorter.app.features.links.repository;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.repository.UserRepository;
import com.linkshorter.app.features.links.model.Link;
import com.linkshorter.app.features.payment.repository.InvoiceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.transaction.Transactional;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class LinkRepositoryTest {

    @Autowired
    private LinkRepository linkRepository;
    @Autowired
    private LinkActivityRepository linkActivityRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;

    private Link link;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("user", "password");
        userRepository.save(user);

        link = new Link();
        link.setLongLink("https://google.com");
        link.setCreationDate(java.sql.Date.valueOf(LocalDate.of(2020, 10, 14)));
        link.setExpirationDate(java.sql.Date.valueOf(LocalDate.of(2020, 10, 15)));
    }

    @AfterEach
    void tearDown() {
        invoiceRepository.deleteAll();
        linkActivityRepository.deleteAll();
        linkRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void shouldDeleteLink() {
        linkRepository.save(link);

        assertDoesNotThrow(() -> linkRepository.delete(link));
        assertEquals(0, linkRepository.count());
    }

    @Test
    @Transactional
    public void shouldDeleteLinkCreatedByUser() {
        link.setUser(user);
        linkRepository.save(link);

        assertDoesNotThrow(() -> linkRepository.delete(link));
        assertEquals(0, linkRepository.count());
        assertTrue(userRepository.existsUserByUsername(user.getUsername()));
    }

    @Test
    public void shouldCountUserLinks() {
        link.setUser(user);
        linkRepository.save(link);

        long userLinksCount = linkRepository.countLinksByUser(user);
        assertEquals(1, userLinksCount);
    }
}