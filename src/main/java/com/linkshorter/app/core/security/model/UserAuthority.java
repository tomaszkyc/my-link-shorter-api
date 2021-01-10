package com.linkshorter.app.core.security.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.annotations.Type;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.UUID;

@Entity(name="user_authorities")
public class UserAuthority implements GrantedAuthority {

    @Id
    @Type(type="uuid-char")
    @ApiModelProperty(value = "Unikalne id uprawnienia użytkownia")
    private UUID id = UUID.randomUUID();

    @Column
    @ApiModelProperty(value = "Nazwa uprawnienia użytkownika", example = "admin")
    private String authority;

    @JoinColumn(name = "user_id")
    @ManyToOne
    @JsonIgnore
    @ApiModelProperty(value = "Użytkownik, do którego przypisane jest uprawnienie", hidden = false)
    private User user;

    public UserAuthority() {}

    public UserAuthority(User user, String authority) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.authority = authority;
    }

    public UserAuthority(String authority) {
        this.id = UUID.randomUUID();
        this.authority = authority;
    }

    @ApiModelProperty(name = "Unikalne id uprawnienia użytkownia")
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @ApiModelProperty(name = "Nazwa uprawnienia użytkownika")
    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


}
