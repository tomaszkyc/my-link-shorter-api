package com.linkshorter.app.features.payment.provider.service;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.repository.UserRepository;
import com.linkshorter.app.features.links.validator.URLValidator;
import com.linkshorter.app.features.payment.model.Invoice;
import com.linkshorter.app.features.payment.model.Payment;
import com.linkshorter.app.features.payment.repository.InvoiceRepository;
import com.linkshorter.app.features.payment.repository.PaymentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TPayPaymentProviderServiceTest {

    @Autowired
    private TPayPaymentProviderService paymentProviderService;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    private Payment payment;
    private User user;
    private Invoice invoice;

    @Autowired
    private URLValidator urlValidator;

    @BeforeEach
    void setUp() {
        prepareUserObject();
        preparePaymentObject();
        initInvoice();

        userRepository.save(user);
        paymentRepository.save(payment);
        invoice.setPayment(payment);
        invoiceRepository.save(invoice);
        payment = paymentRepository.findById(payment.getId()).get();
    }

    @AfterEach
    void tearDown() {
        invoiceRepository.deleteAll();
    }

    private void initInvoice() {
        invoice = Invoice.builder().creationDate(new Date()).netAmount(BigDecimal.valueOf(9.99)).sellerName("Some name").sellerAddress("Some address")
                .sellerTaxIdentifier("Some tax identifier").invoiceNumber("Some invoice number").grossAmount(BigDecimal.valueOf(9.99))
                .taxAmount(BigDecimal.valueOf(0L))
                .invoiceDescription("Some invoice description")
                .user(user).build();
    }

    private void preparePaymentObject() {
        payment = Payment.builder().paymentDescription("Some payment description")
                .amount(BigDecimal.valueOf(9.99)).build();
        invoice = Invoice.builder().user(user).build();
    }


    private void prepareUserObject() {
        if (user == null) {
            String testEmail = "some-email@somedomain.com";
            user = new User(testEmail, "somepassword");
            user.setEmail(testEmail);
            user.setFullName("John Smith");
        }
    }

    @Test
    public void shouldNotCreatePaymentLinkFromNullPaymentObject() {
        assertThrows(Exception.class, () -> paymentProviderService.createPaymentLink(null));
    }

    @Test
    public void shouldNotReturnNullIfCreatedLinkIsValidObject() {
        try {
            String link = paymentProviderService.createPaymentLink(payment);
            assertNotNull(link);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldValidateReturnedPaymentUrlWithApacheUrlValidator() {
        try {
            String link = paymentProviderService.createPaymentLink(payment);
            assertNotNull(link);
            boolean isLinkValid = urlValidator.isValid(link);
            assertTrue(isLinkValid);
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldThrowAnExceptionIfUserIsNullInPayment() {
        User fetchedUser = payment.getInvoice().getUser();
        assertNotNull(fetchedUser);
    }

    @Test
    public void shouldCreateValidEncodedLink() {
        try {
            String link = paymentProviderService.createPaymentLink(payment);
            assertNotNull(link);
            System.out.println("link: " + link);
            boolean isLinkValid = urlValidator.isValid(link);
            assertTrue(isLinkValid);
            assertTrue(link.contains("tpay"));
            assertTrue(link.contains("id="));
            assertTrue(link.contains("kwota="));
            assertTrue(link.contains("opis="));
            assertTrue(link.contains("email="));
            assertTrue(link.contains("nazwisko="));
            assertTrue(link.contains("crc="));
            assertTrue(link.contains("md5sum="));
            assertTrue(link.contains("pow_url="));
            assertTrue(link.contains("pow_url_blad="));
        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldAutowireTPayCustomParameters() {
        Map<String, String> paymentParameters = paymentProviderService.getPaymentProviderParameters();
        assertNotNull(paymentParameters);
    }

    @Test
    public void shouldCalculateProperMd5Sum() {
        String properGeneratedMd5Sum = "3e2af9f5ff28cfaaf1b8b76ef5127c0b";
        payment = Payment.builder().amount(BigDecimal.valueOf(9.99))
                .id(UUID.fromString("0f779a05-2eef-4033-b52f-f98e3a4417f0")).build();

        String generatedMd5Sum = paymentProviderService.calculateMd5Sum(payment);
        assertEquals(properGeneratedMd5Sum, generatedMd5Sum);
    }

}