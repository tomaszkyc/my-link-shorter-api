package com.linkshorter.app.core.security.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginFormDto {

    @ApiModelProperty(value = "Login użytkownika", example = "test-user@linkshorter.com")
    private String username;

    @ApiModelProperty(value = "Hasło użytkownika", notes = "Hasło powinno być min. 8 znakowe, posiadać 1 cyfrę, 1 dużą literę oraz 1 znak specjalny")
    private String password;
}
