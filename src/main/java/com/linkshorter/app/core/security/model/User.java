package com.linkshorter.app.core.security.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.linkshorter.app.features.links.model.Link;
import com.linkshorter.app.features.payment.model.Invoice;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

@Entity(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class User implements Serializable {

    @Id
    @Type(type = "uuid-char")
    @ApiModelProperty(value = "Unikalny id użytkownika")
    private UUID id = UUID.randomUUID();

    @Column
    @ApiModelProperty(value = "Login użytkownika", example = "test-user@linkshorter.com")
    private String username;

    @Column
    @ToString.Exclude
    @ApiModelProperty(value = "Hasło użytkownika", notes = "Hasło powinno być min. 8 znakowe, posiadać 1 cyfrę, 1 dużą literę oraz 1 znak specjalny")
    private String password;

    @Column
    @ApiModelProperty(value = "Status aktywności konta", example = "true")
    private boolean enabled = true;

    @Column(name = "full_name")
    @ApiModelProperty(value = "Nazwa użytkownika", example = "Jan Kowalski")
    private String fullName;

    @Column
    @ApiModelProperty(value = "Email użytkownika", example = "some-mail@gmail.com")
    private String email;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "user_id")
    @ApiModelProperty(value = "Lista uprawnień użytkownika")
    private Set<UserAuthority> userAuthorities = new HashSet<>();

    @OneToMany(mappedBy = "user")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ApiModelProperty(value = "Lista linków stworzonych przez użytkownika")
    private Set<Link> links;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "user")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Setter(value = AccessLevel.NONE)
    @ApiModelProperty(value = "Lista dodatkowych parametrów konfiguracyjnych dla konta użytkownika")
    private Set<UserAdditionalProperty> userAdditionalProperties = new HashSet<>();

    @OneToOne(mappedBy = "user")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @ApiModelProperty(value = "Token do zmiany hasła")
    private UserPasswordResetToken userPasswordResetToken;

    @OneToMany(mappedBy = "user")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JsonIgnore
    @ApiModelProperty(value = "Lista faktur przypisanych do użytkownika")
    private Set<Invoice> invoices;

    public void setUserAdditionalProperties(Set<UserAdditionalProperty> additionalProperties) {
        Set<UserAdditionalProperty> newAdditionalProperties = new HashSet<>(additionalProperties);
        newAdditionalProperties.forEach(property -> property.setUser(this));
        this.userAdditionalProperties.clear();
        this.userAdditionalProperties.addAll(newAdditionalProperties);
    }

    public void setUserAdditionalPropertiesFromMap(Map<String, String> additionalProperties) {
        for (String key : additionalProperties.keySet()) {
            String value = additionalProperties.get(key);
            Optional<UserAdditionalProperty> optionalUserAdditionalProperty = this.userAdditionalProperties.stream().filter(property -> property.equalsTo(key, this)).findAny();
            if (optionalUserAdditionalProperty.isPresent()) {
                UserAdditionalProperty userAdditionalProperty = optionalUserAdditionalProperty.get();
                userAdditionalProperty.setValue(value);
                this.userAdditionalProperties.add(userAdditionalProperty);
            } else {
                UserAdditionalProperty userAdditionalProperty = new UserAdditionalProperty(key, value, this);
                this.userAdditionalProperties.add(userAdditionalProperty);
            }
        }
    }

    public void addUserAdditionalProperty(String key, String value) {
        UserAdditionalProperty userAdditionalProperty = new UserAdditionalProperty(key, value, this);
        userAdditionalProperties.add(userAdditionalProperty);
    }

    public void grantAuthorities(Collection<UserAuthority> authorities) {
        authorities.stream()
                .map(UserAuthority::getAuthority)
                .forEach(this::grantAuthority);
    }

    public void grantAuthority(String authority) {
        UserAuthority userAuthority = new UserAuthority(this, authority);
        this.userAuthorities.add(userAuthority);
    }

    @ApiModelProperty(hidden = true)
    public boolean isAdministrator() {
        Predicate<UserAuthority> hasAdminAuthority = userAuthority -> userAuthority.getAuthority().equals("admin");
        return this.userAuthorities.stream().anyMatch(hasAdminAuthority);
    }

    public User(String username, String password) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.password = password;
    }

    public User(User user) {
        this.id = user.id;
        this.username = user.username;
        this.password = user.password;
        this.enabled = user.enabled;
        this.fullName = user.fullName;
        this.email = user.email;
        this.grantAuthorities(user.getUserAuthorities());
        this.setUserAdditionalProperties(user.getUserAdditionalProperties());
        this.setLinks(user.getLinks());
        this.userPasswordResetToken = null;
        this.invoices = new HashSet<>();
    }

    public User(User user, Collection<UserAuthority> authorities) {
        this.id = user.id;
        this.username = user.username;
        this.password = user.password;
        this.enabled = user.enabled;
        this.fullName = user.fullName;
        this.grantAuthorities(authorities);
    }

    @ApiModelProperty(hidden = true)
    public Map<String, Object> getAuthoritiesAsMap() {
        Map<String, Object> authoritiesMap = new HashMap<>();
        Function<UserAuthority, Boolean> valueForUserAuthority = userAuthority -> Boolean.TRUE;
        Map<String, Object> mapOfGrantedAuthorities = userAuthorities.stream().collect(Collectors.toMap(UserAuthority::getAuthority, valueForUserAuthority));
        authoritiesMap.put("authorities", mapOfGrantedAuthorities);
        return authoritiesMap;
    }

    @ApiModelProperty(hidden = true)
    public Map<String, Object> getUserDetailsAsMap() {
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("fullname", this.getFullName());
        userDetails.put("enabled", this.isEnabled());
        userDetails.put("email", this.getEmail());
        return userDetails;
    }

    @ApiModelProperty(hidden = true)
    public Map<String, String> getUserAdditionalPropertiesAsMap() {
        Map<String, String> userAdditionalProperties = new HashMap<>();
        this.getUserAdditionalProperties().forEach(userAdditionalProperty -> {
            String key = userAdditionalProperty.getKey();
            String value = userAdditionalProperty.getValue();
            userAdditionalProperties.put(key, value);
        });
        return userAdditionalProperties;
    }
}
