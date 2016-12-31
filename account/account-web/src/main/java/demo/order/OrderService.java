package demo.order;

import demo.domain.Service;
import demo.order.domain.Order;
import demo.order.domain.Orders;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestTemplate;

@org.springframework.stereotype.Service
public class OrderService extends Service<Order, Long> {

    private RestTemplate restTemplate;

    public OrderService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public Order get(Long orderId) {
        return restTemplate.getForObject(new UriTemplate("http://order-web/v1/orders/{id}")
                .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                .expand(orderId), Order.class);
    }

    @Override
    public Order create(Order order) {
        return restTemplate.postForObject(new UriTemplate("http://order-web/v1/orders").expand(),
                order, Order.class);
    }

    @Override
    public Order update(Order order) {
        return restTemplate.exchange(new RequestEntity<>(order, HttpMethod.PUT, new UriTemplate
                ("http://order-web/v1/orders/{id}").with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                .expand(order.getIdentity())), Order.class)
                .getBody();
    }

    @Override
    public boolean delete(Long orderId) {
        restTemplate.delete(new UriTemplate("http://order-web/v1/orders/{id}").with("id", TemplateVariable
                .VariableType.PATH_VARIABLE)
                .expand(orderId));
        return true;
    }

    public Orders findOrdersByAccountId(Long accountId) {
        return restTemplate.getForObject(new UriTemplate("http://order-web/v1/orders/search/findOrdersByAccountId")
                .with("accountId", TemplateVariable.VariableType.REQUEST_PARAM)
                .expand(accountId), Orders.class);
    }
}
