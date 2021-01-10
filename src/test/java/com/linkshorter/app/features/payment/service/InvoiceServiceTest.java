package com.linkshorter.app.features.payment.service;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.core.security.repository.UserRepository;
import com.linkshorter.app.features.payment.model.Invoice;
import com.linkshorter.app.features.payment.repository.InvoiceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class InvoiceServiceTest {

    @Autowired
    private InvoiceService invoiceService;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private UserRepository userRepository;
    private Invoice invoice;
    private User user;


    @BeforeEach
    void setUp() {
        initInvoice();
        initUser();

        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        invoiceRepository.deleteAll();
        userRepository.deleteAll();
        this.invoiceService = null;
    }

    @Test
    public void shouldReturnEmptyListIfGivenUserIsNull() {
        User someUser = null;
        List<Invoice> invoices = invoiceService.findAll(someUser);
        assertNotNull(invoices);
        assertEquals(0, invoices.size());
    }

    @Test
    public void shouldReturnInvoicesAssignedToUser() {
        invoice.setUser(user);
        invoiceRepository.save(invoice);

        List<Invoice> invoices = invoiceService.findAll(user);
        assertNotNull(invoices);
        assertEquals(1, invoices.size());
    }

    @Test
    public void shouldReturnAllInvoicesIfUserIsAdmin() {
        invoice.setUser(user);
        invoiceRepository.save(invoice);

        User admin = new User("admin@somedomain.com", "super-secret-password");
        admin.grantAuthority("admin");
        userRepository.save(admin);

        List<Invoice> invoices = invoiceService.findAll(admin);
        assertNotNull(invoices);
        assertEquals(1, invoices.size());
    }

    @Test
    public void shouldCreateInvoiceAndSaveInRepository() {
        try {
            invoice.setUser(user);
            invoice = this.invoiceService.createInvoice(invoice);
            boolean invoiceExistsInRepository = invoiceRepository.existsById(invoice.getId());

            assertTrue(invoiceExistsInRepository);
            assertNotNull(invoice);

        } catch (Exception e) {
            assertNull(e);
        }
    }

    @Test
    public void shouldNotCreateInvoiceAndSaveIfUserIsNull() {
        invoice.setUser(null);
        assertThrows(Exception.class, () -> invoiceService.createInvoice(invoice));

        boolean invoiceExistsInRepository = invoiceRepository.existsById(invoice.getId());
        assertFalse(invoiceExistsInRepository);
    }

    @Test
    public void shouldNotCreateUserIfUserDoesNotExists() {
        User someUser = new User("someuser@somedomain2.com", "super-secret-password");
        invoice.setUser(someUser);
        assertThrows(Exception.class, () -> invoiceService.createInvoice(invoice));

        boolean invoiceExistsInRepository = invoiceRepository.existsById(invoice.getId());
        assertFalse(invoiceExistsInRepository);
    }

    @Test
    public void shouldGenerateInvoiceNumber() {
        String invoiceNumber = invoiceService.generateInvoiceNumber(invoice);

        System.out.println(invoiceNumber);
        assertNotNull(invoiceNumber);
        assertTrue(!invoiceNumber.isBlank());
    }

    @Test
    public void shouldNotGenerateInvoiceNumberForNullableInvoice() {
        assertThrows(Exception.class, () -> invoiceService.generateInvoiceNumber(null));
    }

    @Test
    public void shouldNotGenerateInvoiceNumberWhenIdIsNull() {
        invoice.setId(null);
        assertThrows(Exception.class, () -> invoiceService.generateInvoiceNumber(invoice));
    }

    private void initInvoice() {
        invoice = Invoice.builder().creationDate(new Date()).netAmount(BigDecimal.valueOf(9.99)).sellerName("Some name").sellerAddress("Some address")
                .sellerTaxIdentifier("Some tax identifier").invoiceNumber("Some invoice number").grossAmount(BigDecimal.valueOf(9.99))
                .taxAmount(BigDecimal.valueOf(0L))
                .invoiceDescription("Some invoice description").build();
    }

    private void initUser() {
        user = new User("someuser@somedomain.com", "password");
    }
}