package com.linkshorter.app.features.payment.repository;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.repository.UserRepository;
import com.linkshorter.app.features.payment.model.Invoice;
import com.linkshorter.app.features.payment.model.Payment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import javax.transaction.Transactional;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class InvoiceRepositoryTest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    private Invoice invoice;
    private Payment payment;
    private User user;


    @BeforeEach
    void setUp() {
        payment = Payment.builder().paymentDescription("some description").amount(BigDecimal.valueOf(9.99)).build();
        user = new User("some-user@somedomain.com", "password");
        initInvoice();
        
        paymentRepository.save(payment);
    }

    @AfterEach
    void tearDown() {
        invoiceRepository.deleteAll();
        paymentRepository.deleteAll();
    }

    private void initInvoice() {
        invoice = Invoice.builder().creationDate(new Date()).netAmount(BigDecimal.valueOf(9.99)).sellerName("Some name").sellerAddress("Some address")
                .sellerTaxIdentifier("Some tax identifier").invoiceNumber("Some invoice number").grossAmount(BigDecimal.valueOf(9.99))
                .taxAmount(BigDecimal.valueOf(0L))
                .invoiceDescription("Some invoice description").build();
    }

    @Test
    public void shouldSaveInvoiceWithoutSetUser() {
        assertDoesNotThrow(() -> invoiceRepository.save(invoice));
    }

    @Test
    @Transactional
    public void shouldNotDeleteUserIfDeletingInvoice() {
        userRepository.save(user);
        invoice.setUser(user);
        invoiceRepository.save(invoice);
        invoiceRepository.delete(invoice);

        boolean invoiceExists = invoiceRepository.existsById(invoice.getId());
        boolean userExists = userRepository.existsById(user.getId());

        assertFalse(invoiceExists);
        assertTrue(userExists);
    }

    @Test
    public void shouldSaveInvoiceAndPaymentIfSavingInvoiceOnly() {
        Payment somePayment = Payment.builder().paymentDescription("some description").amount(BigDecimal.valueOf(9.99)).build();
        invoice.setPayment(somePayment);
        invoiceRepository.save(invoice);
        boolean paymentExistsInRepository = paymentRepository.existsById(somePayment.getId());

        assertTrue(paymentExistsInRepository);
    }
}