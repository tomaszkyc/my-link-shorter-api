package com.linkshorter.app.features.payment.service;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.repository.UserRepository;
import com.linkshorter.app.features.payment.model.Invoice;
import com.linkshorter.app.features.payment.model.Payment;
import com.linkshorter.app.features.payment.model.PaymentStatus;
import com.linkshorter.app.features.payment.repository.InvoiceRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class InvoiceService {

    @Value("#{${app.invoice.seller-information}}")
    private Map<String, String> sellerInformation;

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, UserRepository userRepository) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
    }

    public List<Invoice> findAll(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        boolean userIsAdministrator = user.isAdministrator();
        if (userIsAdministrator) {
            return this.invoiceRepository.findAll();
        } else {
            return this.invoiceRepository.findInvoicesByUser(user);
        }
    }

    public Optional<Invoice> findById(UUID invoiceId) {
        return this.invoiceRepository.findById(invoiceId);
    }

    public Invoice createInvoice(Invoice invoice) throws Exception {
        validateInvoice(invoice);
        setSellerInfoForInvoice(invoice);
        createPaymentForInvoice(invoice);
        String invoiceNumber = this.generateInvoiceNumber(invoice);
        invoice.setInvoiceNumber(invoiceNumber);
        return invoiceRepository.save(invoice);
    }

    public String generateInvoiceNumber(Invoice invoice) {
        Objects.requireNonNull(invoice, "Invoice is null");
        Objects.requireNonNull(invoice.getId(), "Invoice id is null");
        StringBuilder invoiceNumber = new StringBuilder();
        Date invoiceCreationDate = invoice.getCreationDate();
        String creationDateFormatted = new SimpleDateFormat("yyyyMM").format(invoiceCreationDate);
        String timestamp = String.valueOf(new Date().getTime());
        String randomDigits = RandomStringUtils.randomNumeric(4);
        return invoiceNumber.append(creationDateFormatted).append(timestamp).append(randomDigits).toString();
    }

    private void setSellerInfoForInvoice(Invoice invoice) {
        Objects.requireNonNull(invoice, "Invoice is null");
        invoice.setSellerName(sellerInformation.get("seller-name"));
        invoice.setSellerAddress(sellerInformation.get("seller-address"));
        invoice.setSellerTaxIdentifier(sellerInformation.get("seller-tax-identifier"));
    }

    private void createPaymentForInvoice(Invoice invoice) {
        Payment payment = Payment.builder().currency(invoice.getCurrency())
                .amount(invoice.getGrossAmount())
                .paymentDescription(invoice.getInvoiceDescription()).build();
        invoice.setPayment(payment);
    }

    private void validateInvoice(Invoice invoice) throws Exception {
        User userAssignedToInvoice = invoice.getUser();
        if (userAssignedToInvoice == null) {
            throw new Exception("Given user is null");
        }
        boolean userExists = userRepository.existsById(userAssignedToInvoice.getId());
        if (!userExists) {
            throw new Exception("User with given id: " + userAssignedToInvoice.getId() + " not exists");
        }
    }

}
