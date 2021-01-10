package com.linkshorter.app.features.links.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity(name = "link_activities")
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class LinkActivity {

    @Id
    @Type(type="uuid-char")
    @ApiModelProperty(value = "Unikalny id aktywności linku")
    private UUID id = UUID.randomUUID();

    @ManyToOne
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    @ApiModelProperty(value = "Link do którego jest przypisana aktywność")
    private Link link;

    @Column
    @ApiModelProperty(value = "Data aktywności")
    private Date activityDate;

    @Column
    @ApiModelProperty(value = "Klasa urządzenia", example = "Desktop")
    private String deviceClass;

    @Column
    @ApiModelProperty(value = "Nazwa urządzenia", example = "Desktop")
    private String deviceName;

    @Column
    @ApiModelProperty(value = "Producent urządzenia", example = "Apple")
    private String deviceBrand;

    @Column
    @ApiModelProperty(value = "Klasa systemu operacyjnego", example = "Desktop")
    private String osClass;

    @Column
    @ApiModelProperty(value = "Nazwa systemu operacyjnego", example = "Windows NT")
    private String osName;

    @Column
    @ApiModelProperty(value = "Wersja systemu operacyjnego", example = "10.0")
    private String osVersion;

    @Column
    @ApiModelProperty(value = "Klasa agenta otwierającego link", example = "Browser")
    private String agentClass;

    @Column
    @ApiModelProperty(value = "Nazwa agenta otwierającego link", example = "Chrome")
    private String agentName;

    @Column
    @ApiModelProperty(value = "Dokładna wersja agenta otwierającego link", example = "79.0.3945.130\n")
    private String agentVersion;

    @Column
    @ApiModelProperty(value = "Główna wersja agenta", example = "79")
    private String agentVersionMajor;

    public LinkActivity(Link link, Date activityDate) {
        this.activityDate = activityDate;
        this.link = link;
    }

}
