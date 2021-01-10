package com.linkshorter.app.core.security.startuphook;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.repository.UserAuthorityRepository;
import com.linkshorter.app.core.security.repository.UserRepository;
import com.linkshorter.app.features.links.repository.LinkActivityRepository;
import com.linkshorter.app.features.links.repository.LinkRepository;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class ApplicationSecurityInitializer implements SmartInitializingSingleton {

    private final UserRepository userRepository;
    private final LinkRepository linkRepository;
    private final LinkActivityRepository linkActivityRepository;
    private final UserAuthorityRepository userAuthorityRepository;
    private final PasswordEncoder encoder;

    @Value("${app.security.admin-password}")
    private String adminPassword;

    public ApplicationSecurityInitializer(UserRepository userRepository, PasswordEncoder encoder,
                                          LinkRepository linkRepository, LinkActivityRepository linkActivityRepository,
                                          UserAuthorityRepository userAuthorityRepository) {
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.linkRepository = linkRepository;
        this.linkActivityRepository = linkActivityRepository;
        this.userAuthorityRepository = userAuthorityRepository;
    }

    @Override
    public void afterSingletonsInstantiated() {

        User admin = new User("admin@linkshorter.com", encoder.encode(adminPassword));
        admin.setEmail("admin@linkshorter.com");
        admin.setFullName("Admin Adminowsky");
        admin.grantAuthority("admin");
        admin.grantAuthority("registered-user");
        admin.addUserAdditionalProperty("sidenav-position", "ltr");
        admin.addUserAdditionalProperty("custom-page-size", "10");
        this.userRepository.save(admin);

        addTestUsers();
        addPremiumTestUser();
    }

    private void addTestUsers() {
        User user = new User("user@linkshorter.com", encoder.encode(adminPassword));
        user.setEmail("user@linkshorter.com");
        user.setFullName("User userowsky");
        user.grantAuthority("registered-user");
        user.addUserAdditionalProperty("sidenav-position", "ltr");
        user.addUserAdditionalProperty("custom-page-size", "10");
        this.userRepository.save(user);
    }

    private void addPremiumTestUser() {
        User premiumUser = new User("premium@linkshorter.com", encoder.encode(adminPassword));
        premiumUser.setEmail("premium@linkshorter.com");
        premiumUser.setFullName("Premium Userovsky");
        premiumUser.grantAuthority("premium-user");
        premiumUser.addUserAdditionalProperty("sidenav-position", "ltr");
        premiumUser.addUserAdditionalProperty("custom-page-size", "10");
        this.userRepository.save(premiumUser);
    }

}
