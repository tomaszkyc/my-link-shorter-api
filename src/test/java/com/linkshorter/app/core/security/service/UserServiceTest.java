package com.linkshorter.app.core.security.service;

import com.linkshorter.app.core.security.exception.UserNotFoundException;
import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.model.UserAdditionalProperty;
import com.linkshorter.app.core.security.model.UserPasswordResetToken;
import com.linkshorter.app.core.security.repository.UserAdditionalPropertyRepository;
import com.linkshorter.app.core.security.repository.UserPasswordResetTokenRepository;
import com.linkshorter.app.core.security.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {

    private static final String TEST_PASSWORD = "TEST_PASSWORD";
    private static final String TEST_USERNAME = "some-random-test-login@linkshorter.com";

    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserAdditionalPropertyRepository userAdditionalPropertyRepository;
    @Autowired
    private UserPasswordResetTokenRepository userPasswordResetTokenRepository;

    private User user;

    @BeforeEach
    void setUp() {
        initOnlyOnce();

        user = new User(TEST_USERNAME, passwordEncoder.encode(TEST_PASSWORD));
    }

    @AfterEach
    void tearDown() {
        System.out.println("Starting clear of all data");
        userPasswordResetTokenRepository.deleteAll();
        userAdditionalPropertyRepository.deleteAll();
        userRepository.deleteAll();
        System.out.println("Finising clear of all data");
    }


    private void initOnlyOnce() {
        if (userService == null) {
            userService = new UserService(userRepository, passwordEncoder, userPasswordResetTokenRepository);
        }
    }

    @Test
    public void shouldFindUserByUsername() {
        userRepository.save(user);
        Optional<User> user = userService.findByUsername(TEST_USERNAME);
        assertTrue(user.isPresent());
    }

    @Test
    public void shouldNotFindUserIfDoesntExist() {
        userRepository.deleteAll();
        Optional<User> user = userService.findByUsername(TEST_USERNAME);
        assertTrue(user.isEmpty());
    }

    @Test
    public void shouldThrowExceptionWhenGivenUsernameIsNull() {
        assertThrows(Exception.class, () -> userService.findByUsername(null));
    }

    @Test
    public void shouldChangeUserPassword() {
        String newPassword = "PASSWORD123";
        userRepository.save(user);
        assertDoesNotThrow(() -> userService.changeUserPassword(user, newPassword));
    }

    @Test
    public void shouldNotChangeUserPasswordWithNullOrEmptyPassword() {
        userRepository.save(user);

        assertThrows(Exception.class, () -> userService.changeUserPassword(user, null));
        assertThrows(Exception.class, () -> userService.changeUserPassword(user, ""));
        assertThrows(Exception.class, () -> userService.changeUserPassword(user, " "));
    }

    @Test
    public void shouldUpdateUserDetails() {
        userRepository.save(user);
        try {
            User updatedUser = userService.updateUser(user);

            assertNotNull(updatedUser);
            assertEquals(user.getUsername(), updatedUser.getUsername());
            assertEquals(user.getFullName(), updatedUser.getFullName());
            assertEquals(user.getEmail(), updatedUser.getEmail());
            assertEquals(user.isEnabled(), updatedUser.isEnabled());
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldNotUpdateUserIfGivenUserIsNUll() {
        assertThrows(NullPointerException.class, () -> userService.updateUser(null));
    }

    @Test
    public void shouldNotUpdateUserIfDoesntExistsInDatabase() {
        assertThrows(Exception.class, () -> userService.updateUser(user));
    }

    @Test
    public void shouldPersistUserAdditionalProperty() {
        String someKey = "someKey";
        String someValue = "someValue";
        Set<UserAdditionalProperty> userAdditionalPropertySet = new HashSet<>();
        userAdditionalPropertySet.add(new UserAdditionalProperty(someKey, someValue, user));
        user.setUserAdditionalProperties(userAdditionalPropertySet);
        userRepository.save(user);

        assertDoesNotThrow(() -> userService.updateUser(user));
        User userFetchedFromRepository = userService.findByUsername(user.getUsername()).orElseThrow();
        assertEquals(userAdditionalPropertySet, userFetchedFromRepository.getUserAdditionalProperties());
        assertEquals(userAdditionalPropertySet.size(), userFetchedFromRepository.getUserAdditionalProperties().size());
    }

    @Test
    public void shouldNotPersistValueIfUserIsNotSet() {
        String someKey = "someKey";
        String someValue = "someValue";
        Set<UserAdditionalProperty> userAdditionalPropertySet = new HashSet<>();
        userAdditionalPropertySet.add(new UserAdditionalProperty(someKey, someValue));

        assertThrows(Exception.class, () -> userAdditionalPropertyRepository.saveAll(userAdditionalPropertySet));
    }

    @Test
    public void shouldGetOnlyUserAdditionalProperties() {
        String someKey = "someKey";
        String someValue = "someValue";
        Set<UserAdditionalProperty> userAdditionalPropertySet = new HashSet<>();
        userAdditionalPropertySet.add(new UserAdditionalProperty(someKey, someValue, user));
        user.setUserAdditionalProperties(userAdditionalPropertySet);

        User otherUser = new User("Otheruser", passwordEncoder.encode(TEST_PASSWORD));
        userRepository.save(user);
        userRepository.save(otherUser);

        assertEquals(user.getUserAdditionalProperties(), userAdditionalPropertyRepository.findAllByUser(user));
        assertEquals(1, userAdditionalPropertyRepository.findAllByUser(user).size());
        assertEquals(0, otherUser.getUserAdditionalProperties().size());
    }

    @Test
    public void shouldAddToUserOneUserAdditionalProperty() {
        String someKey = "someKey";
        String someValue = "someValue";
        user.addUserAdditionalProperty(someKey, someValue);
        userRepository.save(user);

        assertEquals(1, userAdditionalPropertyRepository.findAllByUser(user).size());
    }

    @Test
    public void shouldPersistEmailValue() {
        String someMail = "some-mail@linkshorter.com";
        user.setEmail(someMail);

        userRepository.save(user);

        User userFetchedFromRepository = userRepository.findByUsername(user.getUsername()).orElseThrow();
        assertEquals(someMail, userFetchedFromRepository.getEmail());
    }

    @Test
    public void shouldCreateUserPasswordResetToken() {
        try {
            userRepository.save(user);
            UserPasswordResetToken userPasswordResetToken = userService.createUserPasswordResetToken(user);
            assertNotNull(userPasswordResetToken);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldNotCreateUserPasswordResetTokenForNotExistingUser() {
        User otherUser = new User("someUser", "somePassword");

        assertThrows(Exception.class, () -> userService.createUserPasswordResetToken(null));
        assertThrows(UserNotFoundException.class, () -> userService.createUserPasswordResetToken(otherUser));
    }

    @Test
    public void shouldFindUserPasswordResetTokenById() {
        userRepository.save(user);
        try {
            UserPasswordResetToken userPasswordResetToken = userService.createUserPasswordResetToken(user);
            UUID tokenId = userPasswordResetToken.getId();
            boolean tokenExists = userService.findUserPasswordResetTokenById(tokenId).isPresent();

            assertNotNull(userPasswordResetToken);
            assertNotNull(tokenId);
            assertTrue(tokenExists);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldNotFindInvalidUserPasswordResetToken() {
        UUID someRandomPasswordToken = UUID.randomUUID();
        boolean isTokenValid = userService.validateUserPasswordResetTokenById(someRandomPasswordToken);

        assertFalse(isTokenValid);
    }

    @Test
    public void shouldNotDeleteUserIfDeletingUserPasswordResetToken() {
        try {
            userRepository.save(user);
            UserPasswordResetToken userPasswordResetToken = userService.createUserPasswordResetToken(user);
            userPasswordResetTokenRepository.delete(userPasswordResetToken);

            boolean userExists = userRepository.existsUserByUsername(user.getUsername());
            boolean userPasswordResetTokenExists = userPasswordResetTokenRepository.existsById(userPasswordResetToken.getId());
            assertTrue(userExists);
            assertFalse(userPasswordResetTokenExists);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldCreateUser() {
        try {
            user = userService.createUser(user);
        } catch (Exception e) {
            assertNull(e);
        }
        assertNotNull(user);
        boolean userExists = userRepository.existsUserByUsername(user.getUsername());
        assertTrue(userExists);
    }

    @Test
    public void shouldThrowExceptionIfUserExists() {
        assertDoesNotThrow(() -> userService.createUser(user));
        assertThrows(Exception.class, () -> userService.createUser(user));
    }

    @Test
    public void shouldActivateUserAccount() {
        user = userRepository.save(user);

        assertDoesNotThrow(() -> userService.activateUserAccount(user.getId()));
        Optional<User> userFetchedFromDatabase = userService.findById(user.getId());

        assertNotNull(userFetchedFromDatabase);
        assertTrue(userFetchedFromDatabase.isPresent());
        assertTrue(userFetchedFromDatabase.get().isEnabled());
    }

    @Test
    public void shouldCreateUserTokenConnectedWithUser() {
        userRepository.save(user);
        try {
            UserPasswordResetToken userPasswordResetToken = userService.createUserPasswordResetToken(user);

            assertNotNull(userPasswordResetToken);
            assertNotNull(user.getUserPasswordResetToken());
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldCreateOneTokenAndAnotherOne() {
        userRepository.save(user);
        try {
            UserPasswordResetToken userPasswordResetToken = userService.createUserPasswordResetToken(user);
            UserPasswordResetToken anotherUserPasswordResetToken = userService.createUserPasswordResetToken(user);
            assertNotNull(userPasswordResetToken);
            assertNotNull(user.getUserPasswordResetToken());
            assertNotNull(anotherUserPasswordResetToken);
            assertNotNull(user.getUserPasswordResetToken());
        } catch (Exception tokenGenerationException) {
            assertNull(tokenGenerationException);
        }
    }
}