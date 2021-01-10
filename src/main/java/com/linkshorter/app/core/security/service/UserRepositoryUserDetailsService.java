package com.linkshorter.app.core.security.service;


import com.linkshorter.app.core.security.model.BridgeUser;
import com.linkshorter.app.core.security.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserRepositoryUserDetailsService implements UserDetailsService {
    private final UserRepository users;

    public UserRepositoryUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        return this.users.findByUsername(username)
                .map(BridgeUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("Can't find user with username: " + username));
    }

}
