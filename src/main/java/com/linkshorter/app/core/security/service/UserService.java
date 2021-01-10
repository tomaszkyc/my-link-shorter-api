package com.linkshorter.app.core.security.service;

import com.linkshorter.app.core.security.exception.UserNotFoundException;
import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.model.UserPasswordResetToken;
import com.linkshorter.app.core.security.repository.UserPasswordResetTokenRepository;
import com.linkshorter.app.core.security.repository.UserRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPasswordResetTokenRepository userPasswordResetTokenRepository;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder
            , UserPasswordResetTokenRepository userPasswordResetTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userPasswordResetTokenRepository = userPasswordResetTokenRepository;
    }

    public Optional<User> findByUsername(String username) {
        Objects.requireNonNull(username, "Given username cannot be null");
        return userRepository.findByUsername(username);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public void changeUserPassword(User user, String newPassword) throws Exception {
        Objects.requireNonNull(user);
        validateUserPassword(newPassword);
        UUID userId = user.getId();
        Optional<User> userFetchedFromRepository = userRepository.findById(userId);
        if (userFetchedFromRepository.isEmpty()) {
            throw new Exception("User with given id: " + userId + " doesn't exists.");
        }

        User userToUpdate = userFetchedFromRepository.get();
        String encodedPassword = passwordEncoder.encode(newPassword);
        userToUpdate.setPassword(encodedPassword);

        userRepository.save(userToUpdate);
    }

    public User updateUser(User user) throws Exception {
        Objects.requireNonNull(user, "User object cannot be null");
        Objects.requireNonNull(user.getId(), "User id cannot be null");
        UUID userId = user.getId();
        User userFetchedFromDatabase = userRepository.findById(userId)
                .orElseThrow(() -> new Exception("Cannot find user by id: " + userId));

        updateSelectedUserFields(user, userFetchedFromDatabase);
        return userRepository.save(userFetchedFromDatabase);
    }

    private void updateSelectedUserFields(User sourceUser, User destinationUser) {
        destinationUser.setEmail(sourceUser.getEmail());
        destinationUser.setEnabled(sourceUser.isEnabled());
        destinationUser.setFullName(sourceUser.getFullName());
        destinationUser.setUsername(sourceUser.getUsername());
        destinationUser.setUserAdditionalProperties(sourceUser.getUserAdditionalProperties());
        destinationUser.getUserAuthorities().clear();
        destinationUser.grantAuthorities(sourceUser.getUserAuthorities());
    }

    private void validateUserPassword(String password) throws Exception {
        if (StringUtils.isBlank(password)) {
            throw new Exception("Password cannot be null or empty");
        }
    }

    public UserPasswordResetToken createUserPasswordResetToken(User user) throws Exception {
        Objects.requireNonNull(user, "Given user object cannot be null");
        validateIfUserExists(user);
        UserPasswordResetToken userPasswordResetToken = new UserPasswordResetToken(user);
        userPasswordResetToken = userPasswordResetTokenRepository.save(userPasswordResetToken);
        this.userRepository.save(user);
        return userPasswordResetToken;
    }

    public Optional<UserPasswordResetToken> findUserPasswordResetTokenById(UUID id) {
        return userPasswordResetTokenRepository.findById(id);
    }

    public boolean validateUserPasswordResetTokenById(UUID id) {
        Optional<UserPasswordResetToken> optionalUserPasswordResetToken = userPasswordResetTokenRepository.findById(id);
        if (optionalUserPasswordResetToken.isPresent()) {
            UserPasswordResetToken userPasswordResetToken = optionalUserPasswordResetToken.get();
            Date userPasswordTokenExpirationDate = userPasswordResetToken.getExpirationDate();
            Date now = new Date();
            return now.before(userPasswordTokenExpirationDate);
        }
        return false;
    }

    @Transactional
    public void deleteUserPasswordResetTokenById(UUID id) {
        userPasswordResetTokenRepository.deleteById(id);
    }

    public User createUser(User user) throws Exception {
        log.trace("Started creating account for user: {}", user);
        String username = user.getUsername();
        String password = user.getPassword();
        boolean userExistsWithGivenUsername = userRepository.existsUserByUsername(username);
        if (userExistsWithGivenUsername) {
            throw new Exception("Użytkownik z podaną nazwą użytkownika: " + username + " istnieje już w systemie");
        }

        log.trace("User with username {} doesn't exist in database. Starting creating user");
        user.setPassword(passwordEncoder.encode(password));
        user.grantAuthority("registered-user");
        user.setEnabled(false);
        addUserDefaultUserAdditionalProperties(user);
        user = userRepository.save(user);
        log.info("User with username: {} successfully created", user.getUsername());
        return user;
    }

    public void activateUserAccount(UUID userId) throws Exception {
        User user = userRepository.findById(userId).orElseThrow(() -> new Exception("Użytkownik o id: " + userId + " nie istnieje"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    private void addUserDefaultUserAdditionalProperties(User user) {
        user.addUserAdditionalProperty("sidenav-position", "ltr");
        user.addUserAdditionalProperty("custom-page-size", "10");
    }

    private void validateIfUserExists(User user) throws UserNotFoundException {
        UUID userId = user.getId();
        boolean userExists = userRepository.existsById(userId);
        if (!userExists) {
            throw new UserNotFoundException(userId);
        }
    }


}
