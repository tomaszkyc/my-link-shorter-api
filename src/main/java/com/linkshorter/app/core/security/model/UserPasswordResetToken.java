package com.linkshorter.app.core.security.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity(name = "user_password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPasswordResetToken {

    private static final int EXPIRATION_TIME_IN_MINUTES = 60 * 24;

    @Id
    @Type(type = "uuid-char")
    @ApiModelProperty(value = "Unikalny id tokenu do resetowania hasła")
    private UUID id = UUID.randomUUID();

    @OneToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @ApiModelProperty(value = "Id użytkownika, dla którego wygenerowany jest token")
    private User user;

    @ApiModelProperty(value = "Data wygaśnięcia tokenu do zmiany hasła", notes = "Domyślnie token jest aktywny 24h od momentu jego utworzenia")
    private Date expirationDate;

    public UserPasswordResetToken(User user) {
        this.user = user;
        user.setUserPasswordResetToken(this);
        LocalDateTime localDateTime = LocalDateTime.now().plusMinutes(EXPIRATION_TIME_IN_MINUTES);
        expirationDate = java.sql.Timestamp.valueOf(localDateTime);
    }

    @ApiModelProperty(value = "pokazuje, czy dany token jest ważny dla daty sprawdzenia", example = "true")
    public boolean isValid() {
        return expirationDate.after(new Date());
    }
}
