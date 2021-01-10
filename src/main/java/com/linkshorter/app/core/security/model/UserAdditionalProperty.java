package com.linkshorter.app.core.security.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.UUID;

@Entity(name = "users_additional_properties")
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class UserAdditionalProperty {

    @Id
    @Type(type="uuid-char")
    @ApiModelProperty(value = "Unikalny id właściwości")
    private UUID id = UUID.randomUUID();

    @Column(name = "additional_property_key")
    @ApiModelProperty(value = "Klucz właściwości", example = "sidenav-position")
    private String key;

    @Column(name = "additional_property_value")
    @ApiModelProperty(value = "Wartość właściwości", example = "ltr")
    private String value;

    @ManyToOne(optional = false)
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @ApiModelProperty(value = "Użytkownik, do którego przypisana jest właściwość")
    private User user;

    public UserAdditionalProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public UserAdditionalProperty(String key, String value, User user) {
        this(key, value);
        this.setUser(user);
    }

    @ApiModelProperty(hidden = true)
    public boolean equalsTo(String key, User user) {
        return this.getKey().equals(key) && this.getUser().equals(user);
    }

}
