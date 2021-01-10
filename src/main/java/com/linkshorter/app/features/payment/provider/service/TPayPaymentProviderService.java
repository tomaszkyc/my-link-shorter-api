package com.linkshorter.app.features.payment.provider.service;

import com.linkshorter.app.core.security.model.User;
import com.linkshorter.app.features.payment.model.Payment;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Objects;

@Service
public class TPayPaymentProviderService implements PaymentProviderService {

    private static final String QUERY_PARAMETERS_ENCODING = "UTF-8";

    @Value("#{${app.payment.tpay}}")
    private Map<String, String> paymentProviderParameters;

    @Override
    public String createPaymentLink(Payment payment) throws Exception {
        validatePaymentObject(payment);
        return createLink(payment);
    }

    @Override
    public Map<String, String> getPaymentProviderParameters() {
        return paymentProviderParameters;
    }

    private void validatePaymentObject(Payment payment) {
        Objects.requireNonNull(payment, "Payment object is null");
    }

    private String createLink(Payment payment) throws Exception {
        validatePaymentObject(payment);
        String paymentProviderBaseUrl = paymentProviderParameters.get("payment-base-url");
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(paymentProviderBaseUrl);
        addQueryParametersToUriComponentBuilder(uriComponentsBuilder, payment);
        UriComponents uriComponents = uriComponentsBuilder.build(true);
        return uriComponents.toUri().toString();
    }

    private void addQueryParametersToUriComponentBuilder(UriComponentsBuilder uriComponentsBuilder, Payment payment) throws UnsupportedEncodingException {
        uriComponentsBuilder.queryParam("id", paymentProviderParameters.get("payment-client-id"));
        uriComponentsBuilder.queryParam("kwota", payment.getAmount().toString());
        uriComponentsBuilder.queryParam("opis", URLEncoder.encode(payment.getPaymentDescription(), QUERY_PARAMETERS_ENCODING));
        User user = payment.getInvoice().getUser();
        uriComponentsBuilder.queryParam("email", user.getEmail());
        uriComponentsBuilder.queryParam("nazwisko", URLEncoder.encode(user.getFullName(), QUERY_PARAMETERS_ENCODING));
        uriComponentsBuilder.queryParam("crc", URLEncoder.encode(payment.getCrcCode(), QUERY_PARAMETERS_ENCODING));

        String md5sum = this.calculateMd5Sum(payment);
        uriComponentsBuilder.queryParam("md5sum", md5sum);

        String paymentSuccessEndpoint = new String(paymentProviderParameters.get("payment-success-endpoint").replaceAll("@crc@", payment.getCrcCode()));
        String paymentFailureEndpoint = new String(paymentProviderParameters.get("payment-failure-endpoint").replaceAll("@crc@", payment.getCrcCode()));
        uriComponentsBuilder.queryParam("pow_url", URLEncoder.encode(paymentSuccessEndpoint, QUERY_PARAMETERS_ENCODING));
        uriComponentsBuilder.queryParam("pow_url_blad", URLEncoder.encode(paymentFailureEndpoint, QUERY_PARAMETERS_ENCODING));
    }

    public String calculateMd5Sum(Payment payment) {
        String paymentClientId = paymentProviderParameters.get("payment-client-id");
        String amount = payment.getAmount().toString();
        String crcCode = payment.getId().toString();
        String paymentClientSecret = paymentProviderParameters.get("payment-client-secret");
        String concatenatedTextForMd5Sum = String.format("%s%s%s%s", paymentClientId, amount, crcCode, paymentClientSecret);
        return DigestUtils.md5Hex(concatenatedTextForMd5Sum);
    }
}
