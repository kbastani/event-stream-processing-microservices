package demo.order.domain;

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
public class OrderService extends Service<Order, Long> {

    private final Logger log = Logger.getLogger(this.getClass());
    private final RestTemplate restTemplate;

    public OrderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Order get(Long orderId) {
        Order result;
        try {
            result = restTemplate.getForObject(new UriTemplate("http://order-web/v1/orders/{id}")
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                    .expand(orderId), Order.class);
        } catch (RestClientResponseException ex) {
            log.error("Get order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    @Override
    public Order create(Order order) {
        Order result;
        try {
            result = restTemplate.postForObject(new UriTemplate("http://order-web/v1/orders").expand(),
                    order, Order.class);
        } catch (RestClientResponseException ex) {
            log.error("Create order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    @Override
    public Order update(Order order) {
        Order result;
        try {
            result = restTemplate.exchange(new RequestEntity<>(order, HttpMethod.PUT,
                    new UriTemplate("http://order-web/v1/orders/{id}")
                            .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                            .expand(order.getIdentity())), Order.class).getBody();
        } catch (RestClientResponseException ex) {
            log.error("Update order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
    }

    @Override
    public boolean delete(Long orderId) {
        try {
            restTemplate.delete(new UriTemplate("http://order-web/v1/orders/{id}")
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE).expand(orderId));
        } catch (RestClientResponseException ex) {
            log.error("Delete order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return true;
    }

    public Orders findOrdersByAccountId(Long accountId) {
        Orders result;
        try {
            result = restTemplate
                    .getForObject(new UriTemplate("http://order-web/v1/orders/search/findOrdersByAccountId")
                            .with("accountId", TemplateVariable.VariableType.REQUEST_PARAM)
                            .expand(accountId), Orders.class);
        } catch (RestClientResponseException ex) {
            log.error("Delete order failed", ex);
            throw new IllegalStateException(getHttpStatusMessage(ex), ex);
        }

        return result;
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
