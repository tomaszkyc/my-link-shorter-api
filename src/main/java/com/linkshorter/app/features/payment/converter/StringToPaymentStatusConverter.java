package com.linkshorter.app.features.payment.converter;

import com.linkshorter.app.features.payment.model.PaymentStatus;
import org.springframework.core.convert.converter.Converter;

public class StringToPaymentStatusConverter implements Converter<String, PaymentStatus> {
    @Override
    public PaymentStatus convert(String source) {
        return PaymentStatus.parse(source);
    }
}
