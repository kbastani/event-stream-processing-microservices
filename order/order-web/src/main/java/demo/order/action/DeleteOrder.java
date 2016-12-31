package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
import demo.order.domain.OrderModule;
import demo.payment.domain.Payment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.function.Consumer;

/**
 * Processes a {@link Payment} for an {@link Order}.
 *
 * @author Kenny Bastani
 */
@Service
public class DeleteOrder extends Action<Order> {

    private RestTemplate restTemplate;

    public DeleteOrder(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Consumer<Order> getConsumer() {
        return (order) -> {
            // Delete payment
            if (order.getPaymentId() != null) {
                String href = "http://payment-web/v1/payments/" + order.getPaymentId();
                restTemplate.delete(href);
            }

            // Delete order
            order.getModule(OrderModule.class)
                    .getDefaultService()
                    .delete(order.getIdentity());
        };
    }
}
