package demo.order.action;

import demo.domain.Action;
import demo.event.OrderEvent;
import demo.event.OrderEventType;
import demo.order.Order;
import demo.order.OrderProvider;
import demo.order.OrderService;
import demo.order.OrderStatus;
import demo.payment.Payment;
import demo.payment.PaymentMethod;
import org.apache.log4j.Logger;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.function.Consumer;

/**
 * Creates a {@link Payment} for an {@link Order}.
 *
 * @author Kenny Bastani
 */
@Service
public class CreatePayment extends Action<Order> {

    private final Logger log = Logger.getLogger(CreatePayment.class);

    private RestTemplate restTemplate;

    public CreatePayment(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Consumer<Order> getConsumer() {
        return order -> {

            OrderService orderService = (OrderService) order.getProvider(OrderProvider.class)
                    .getDefaultService();

            Payment payment = new Payment();

            // Calculate payment amount
            payment.setAmount(order.getLineItems()
                    .stream()
                    .mapToDouble(a -> (a.getPrice() + a.getTax()) * a.getQuantity())
                    .sum());

            // Set payment method
            payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);

            // Create a new request entity
            RequestEntity<Resource<Payment>> requestEntity = RequestEntity.post(
                    URI.create("http://localhost:8082/v1/payments"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaTypes.HAL_JSON)
                    .body(new Resource<>(payment), Resource.class);

            // Update the order entity's status
            Resource paymentResource = restTemplate
                    .exchange(requestEntity, Resource.class)
                    .getBody();

            log.info(paymentResource);

            // Update the status
            order.setStatus(OrderStatus.PAYMENT_CREATED);
            order = orderService.update(order);

            OrderEvent event = new OrderEvent(OrderEventType.PAYMENT_CREATED, order);
            event.add(paymentResource.getLink("self")
                    .withRel("payment"));

            // Trigger the payment created
            order.sendAsyncEvent(event);
        };
    }
}
