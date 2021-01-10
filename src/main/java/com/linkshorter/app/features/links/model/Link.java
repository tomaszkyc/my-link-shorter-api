package com.linkshorter.app.features.links.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.linkshorter.app.core.security.model.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity(name = "links")
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class Link implements Serializable {

    @Id
    @Type(type = "uuid-char")
    @ApiModelProperty(value = "Unikalne id linku")
    private UUID id = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.EAGER)
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @ApiModelProperty(value = "Użytkownik, który stworzył link. W przypadku, gdy link stworzył użytkownik niezalogowany pole ma wartość null")
    private User user;

    @Column(unique = true)
    @ApiModelProperty(value = "Krótki link")
    private String shortLink;

    @Column
    @ApiModelProperty(value = "Długi link, do którego zostanie przekierowana osoba po otworzeniu krótkiego linku")
    private String longLink;

    @Column
    @ApiModelProperty(value = "Data utworzenia linku")
    private Date creationDate;

    @Column
    @ApiModelProperty(value = "Data wygaśnięcia linku")
    private Date expirationDate;

    @Column
    @ApiModelProperty(value = "Status aktywności linku", example = "false")
    private boolean active;

    @OneToMany(mappedBy = "link")
    @EqualsAndHashCode.Exclude
    @ApiModelProperty(value = "Dane dotyczące aktywności linku")
    private Set<LinkActivity> linkActivities = new HashSet<>();

    public Link(Link otherLink) {
        this.id = UUID.randomUUID();
        this.user = otherLink.getUser();
        this.shortLink = otherLink.getShortLink();
        this.longLink = otherLink.getLongLink();
        this.creationDate = otherLink.getCreationDate();
        this.expirationDate = otherLink.getExpirationDate();
        this.active = otherLink.isActive();
        this.linkActivities = new HashSet<>();
    }

}
