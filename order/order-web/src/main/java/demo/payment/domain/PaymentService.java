package demo.payment.domain;

import com.fasterxml.jackson.databind.ObjectMapper;
import demo.domain.Service;
import org.apache.log4j.Logger;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@org.springframework.stereotype.Service
public class PaymentService extends Service<Payment, Long> {

    private final Logger log = Logger.getLogger(this.getClass());
    private final RestTemplate restTemplate;

    public PaymentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Payment get(Long paymentId) {
        Payment result;
        try {
            result = restTemplate.getForObject(new UriTemplate("http://payment-web/v1/payments/{id}")
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                    .expand(paymentId), Payment.class);
        } catch (RestClientResponseException ex) {
            log.error("Get payment failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    @Override
    public Payment create(Payment payment) {
        Payment result;
        try {
            result = restTemplate.postForObject(new UriTemplate("http://payment-web/v1/payments").expand(),
                    payment, Payment.class);
        } catch (RestClientResponseException ex) {
            log.error("Create payment failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    @Override
    public Payment update(Payment payment) {
        Payment result;
        try {
            result = restTemplate.exchange(new RequestEntity<>(payment, HttpMethod.PUT,
                    new UriTemplate("http://payment-web/v1/payments/{id}")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(payment.getIdentity())), Payment.class).getBody();
        } catch (RestClientResponseException ex) {
            log.error("Update payment failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    @Override
    public boolean delete(Long paymentId) {
        try {
            restTemplate.delete(new UriTemplate("http://payment-web/v1/payments/{id}")
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE).expand(paymentId));
        } catch (RestClientResponseException ex) {
            log.error("Delete payment failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return true;
    }

    private String getHttpStatusMessage(RestClientResponseException ex) {
        Map<String, String> errorMap = new HashMap<>();
        try {
            errorMap = new ObjectMapper()
                    .readValue(ex.getResponseBodyAsString(), errorMap
                            .getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return errorMap.getOrDefault("message", null);
    }
}
