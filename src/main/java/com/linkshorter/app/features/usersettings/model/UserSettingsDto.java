package com.linkshorter.app.features.usersettings.model;


import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsDto {
    private String fullname;
    private String email;
    private String password;
    private Map<String, String> userAdditionalProperties;
}
