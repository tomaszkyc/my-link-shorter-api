package com.linkshorter.app.features.contactus.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactFormDto {
    private String name;
    private String email;
    private String message;
    private Date creationDate = new Date();


    public LocalDateTime getCreationDateAsLocalDateTime() {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(creationDate.toInstant(), ZoneId.systemDefault());
        return localDateTime;
    }
}
