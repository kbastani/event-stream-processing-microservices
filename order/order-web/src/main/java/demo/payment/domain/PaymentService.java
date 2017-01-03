package demo.payment.domain;

import demo.domain.Service;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;

@org.springframework.stereotype.Service
public class PaymentService extends Service<Payment, Long> {

    private RestTemplate restTemplate;

    public PaymentService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Payment get(Long paymentId) {
        return restTemplate.getForObject(new UriTemplate("http://payment-web/v1/payments/{id}")
                .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                .expand(paymentId), Payment.class);
    }

    @Override
    public Payment create(Payment payment) {
        return restTemplate.postForObject(new UriTemplate("http://payment-web/v1/payments").expand(),
                payment, Payment.class);
    }

    @Override
    public Payment update(Payment payment) {
        return restTemplate.exchange(new RequestEntity<>(payment, HttpMethod.PUT, new UriTemplate
                ("http://payment-web/v1/payments/{id}").with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                .expand(payment.getIdentity())), Payment.class)
                .getBody();
    }

    @Override
    public boolean delete(Long paymentId) {
        restTemplate.delete(new UriTemplate("http://payment-web/v1/payments/{id}").with("id", TemplateVariable
                .VariableType.PATH_VARIABLE)
                .expand(paymentId));
        return true;
    }
}
