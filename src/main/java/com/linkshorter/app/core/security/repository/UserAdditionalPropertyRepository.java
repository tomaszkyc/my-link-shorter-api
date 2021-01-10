package com.linkshorter.app.core.security.repository;

import com.linkshorter.app.core.security.model.UserAdditionalProperty;
import com.linkshorter.app.core.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface UserAdditionalPropertyRepository extends JpaRepository<UserAdditionalProperty, UUID> {
    Optional<UserAdditionalProperty> findByUserAndKey(User user, String additionalPropertyKey);

    Set<UserAdditionalProperty> findAllByUser(User user);
}
