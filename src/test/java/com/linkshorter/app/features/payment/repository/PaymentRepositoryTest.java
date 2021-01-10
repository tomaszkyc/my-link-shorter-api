package com.linkshorter.app.features.payment.repository;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.repository.UserRepository;
import com.linkshorter.app.features.payment.model.Invoice;
import com.linkshorter.app.features.payment.model.Payment;
import com.linkshorter.app.features.payment.model.PaymentStatus;
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
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    private Payment payment = null;
    private User user;
    private Invoice invoice;

    @BeforeEach
    void setUp() {
        user = new User("some-user@somedomain.com", "password");
        payment = Payment.builder().paymentDescription("Some payment description")
                    .amount(new BigDecimal(9.99)).build();

        userRepository.save(user);

        initInvoice();
    }

    @AfterEach
    @Transactional
    void tearDown() {
        paymentRepository.deleteAll();
        userRepository.deleteAll();
    }

    private void initInvoice() {
        invoice = Invoice.builder().creationDate(new Date()).netAmount(BigDecimal.valueOf(9.99)).sellerName("Some name").sellerAddress("Some address")
                .sellerTaxIdentifier("Some tax identifier").invoiceNumber("Some invoice number").grossAmount(BigDecimal.valueOf(9.99))
                .taxAmount(BigDecimal.valueOf(0L))
                .invoiceDescription("Some invoice description").build();
    }

    @Test
    public void shouldCreatePayment() {
        assertDoesNotThrow(() -> paymentRepository.save(payment));
    }

    @Test
    public void shouldCreatePaymentWithDefaultCurrencyPln() {
        String defaultCurrency = "PLN";
        assertDoesNotThrow(() -> paymentRepository.save(payment));
        assertEquals(defaultCurrency, payment.getCurrency());
    }

    @Test
    @Transactional
    public void shouldNotDeleteUserIfDeletingPayment() {
        long usersCountBeforePaymentDelete = userRepository.count();
        paymentRepository.save(payment);
        paymentRepository.delete(payment);
        long usersCountAfterPayment = userRepository.count();

        boolean paymentExists = paymentRepository.existsById(payment.getId());
        assertFalse(paymentExists);
        assertEquals(usersCountBeforePaymentDelete, usersCountAfterPayment);
    }

    @Test
    @Transactional
    public void shouldNotDeleteInvoiceIfDeletingPayment() {
        paymentRepository.save(payment);
        invoice.setPayment(payment);
        invoiceRepository.save(invoice);


        invoice.setPayment(null);
        invoiceRepository.save(invoice);


        boolean paymentExists = paymentRepository.existsById(payment.getId());
        boolean invoiceExists = invoiceRepository.existsById(invoice.getId());

        assertFalse(paymentExists);
        assertTrue(invoiceExists);
    }

}