package com.linkshorter.app.features.payment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @Type(type = "uuid-char")
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column
    @NotBlank
    private String paymentDescription;

    @Column
    @NotNull
    private BigDecimal amount;

    @Column
    @Builder.Default
    @NotBlank
    private String currency = "PLN";

    @Column
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @NotNull
    private PaymentStatus paymentStatus = PaymentStatus.CREATED;

    @Column
    @Builder.Default
    @NotNull
    private Date creationDate = new Date();

    @Column
    @Builder.Default
    @NotNull
    private Date paymentDate = new Date();

    @OneToOne(mappedBy = "payment", fetch = FetchType.EAGER)
    @JsonIgnore
    private Invoice invoice;

    public String getCrcCode() {
        return this.id.toString();
    }
}
