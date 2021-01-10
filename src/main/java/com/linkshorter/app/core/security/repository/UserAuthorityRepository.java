package com.linkshorter.app.core.security.repository;

import com.linkshorter.app.core.security.model.UserAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserAuthorityRepository extends JpaRepository<UserAuthority, UUID> {
}
