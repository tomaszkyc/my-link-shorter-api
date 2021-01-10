package com.linkshorter.app.features.payment.repository;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.features.payment.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    List<Invoice> findInvoicesByUser(User user);
}
