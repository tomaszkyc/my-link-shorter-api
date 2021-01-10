package com.linkshorter.app.features.payment.provider.service;

import com.linkshorter.app.features.payment.model.Payment;

import java.util.Map;

public interface PaymentProviderService {
    String createPaymentLink(Payment payment) throws Exception;

    Map<String, String> getPaymentProviderParameters();
}
