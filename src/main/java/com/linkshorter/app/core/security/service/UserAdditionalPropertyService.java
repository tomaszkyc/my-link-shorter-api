package com.linkshorter.app.core.security.service;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.model.UserAdditionalProperty;
import com.linkshorter.app.core.security.repository.UserAdditionalPropertyRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserAdditionalPropertyService {

    private final UserAdditionalPropertyRepository userAdditionalPropertyRepository;

    public UserAdditionalPropertyService(UserAdditionalPropertyRepository userAdditionalPropertyRepository) {
        this.userAdditionalPropertyRepository = userAdditionalPropertyRepository;
    }

    public Optional<UserAdditionalProperty> findByUserAndKey(User user, String additionalPropertyKey) {
        return userAdditionalPropertyRepository.findByUserAndKey(user, additionalPropertyKey);
    }
}
