package com.linkshorter.app.core.security.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Data
@EqualsAndHashCode(callSuper = false)
public class BridgeUser extends User implements UserDetails {

    public BridgeUser(User user, Collection<UserAuthority> authorities) {
        super(user, authorities);
    }

    public BridgeUser(User user) {
        super(user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return super.getUserAuthorities();
    }

    @Override
    public boolean isAccountNonExpired() {
        return super.isEnabled();
    }

    @Override
    public boolean isAccountNonLocked() {
        return super.isEnabled();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return super.isEnabled();
    }
}
