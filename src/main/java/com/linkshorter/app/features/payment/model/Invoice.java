package com.linkshorter.app.features.payment.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.linkshorter.app.core.security.model.User;
import lombok.*;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity(name = "invoices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @Type(type = "uuid-char")
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column
    @NotBlank
    private String invoiceNumber;

    @Column
    @NotNull
    @Builder.Default
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date creationDate = new Date();

    @Column
    @NotBlank
    private String sellerName;

    @Column
    @NotBlank
    private String sellerAddress;

    @Column
    @NotBlank
    private String sellerTaxIdentifier;

    @Column
    @NotBlank
    private String invoiceDescription;

    @Column
    @Builder.Default
    @NotBlank
    private String currency = "PLN";

    @Column
    @NotNull
    private BigDecimal netAmount;

    @Column
    @NotNull
    private BigDecimal taxAmount;

    @Column
    @NotNull
    private BigDecimal grossAmount;

    @ManyToOne(fetch = FetchType.EAGER)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private User user;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    public Invoice(Invoice originalInvoice) {
        this.id = UUID.randomUUID();
        this.invoiceNumber = originalInvoice.getInvoiceNumber();
        this.creationDate = originalInvoice.getCreationDate();
        this.sellerName = originalInvoice.getSellerName();
        this.sellerAddress = originalInvoice.getSellerAddress();
        this.sellerTaxIdentifier = originalInvoice.getSellerTaxIdentifier();
        this.invoiceDescription = originalInvoice.getInvoiceDescription();
        this.currency = originalInvoice.getCurrency();
        this.netAmount = originalInvoice.getNetAmount();
        this.taxAmount = originalInvoice.getTaxAmount();
        this.grossAmount = originalInvoice.getGrossAmount();
        this.user = originalInvoice.getUser();
        this.payment = originalInvoice.getPayment();
    }
}
