package com.linkshorter.app.features.payment.model;

public enum PaymentStatus {
    CREATED,
    PAID,
    CANCEL;


    public static PaymentStatus parse(String source) {
        return PaymentStatus.valueOf(source);
    }
}
