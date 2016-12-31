package demo.order.action;

import demo.domain.Action;
import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import demo.order.domain.Order;
import demo.order.domain.OrderModule;
import demo.order.domain.OrderService;
import demo.order.domain.OrderStatus;
import demo.payment.domain.Payment;
import demo.payment.domain.PaymentMethod;
import org.apache.log4j.Logger;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
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
            Assert.isTrue(order.getPaymentId() == null, "Payment has already been created");
            Assert.isTrue(order.getStatus() == OrderStatus.ACCOUNT_CONNECTED, "Account must be connected first");

            OrderService orderService = order.getProvider(OrderModule.class)
                    .getDefaultService();

            Payment payment = new Payment();

            // Calculate payment amount
            payment.setAmount(order.calculateTotal());

            // Set payment method
            payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);

            // Create a new request entity
            RequestEntity<Resource<Payment>> requestEntity = RequestEntity.post(
                    URI.create("http://payment-web/v1/payments"))
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
