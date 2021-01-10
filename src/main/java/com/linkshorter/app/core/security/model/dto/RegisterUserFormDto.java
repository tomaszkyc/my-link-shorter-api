package com.linkshorter.app.core.security.model.dto;


import com.linkshorter.app.core.security.model.User;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserFormDto {

    @ApiModelProperty(value = "Login użytkownika", example = "test-user@linkshorter.com")
    private String username;

    @ApiModelProperty(value = "Hasło użytkownika", notes = "Hasło powinno być min. 8 znakowe, posiadać 1 cyfrę, 1 dużą literę oraz 1 znak specjalny")
    private String password;

    @ApiModelProperty(value = "Nazwa użytkownika", example = "Jan Kowalski")
    private String fullName;

    @ApiModelProperty(value = "Email użytkownika", example = "some-mail@gmail.com")
    private String email;

    @ApiModelProperty(hidden = true)
    public User toUser() {
        User user = new User(username, password);
        user.setEmail(email);
        user.setFullName(fullName);
        return user;
    }
}
