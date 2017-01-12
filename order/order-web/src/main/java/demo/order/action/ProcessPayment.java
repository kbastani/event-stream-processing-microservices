package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
import demo.order.domain.OrderModule;
import demo.order.domain.OrderService;
import demo.order.domain.OrderStatus;
import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import demo.payment.domain.Payment;
import org.apache.log4j.Logger;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.Arrays;
import java.util.function.Function;

/**
 * Processes a {@link Payment} for an {@link Order}.
 *
 * @author Kenny Bastani
 */
@Service
public class ProcessPayment extends Action<Order> {

    private final Logger log = Logger.getLogger(this.getClass());

    public Function<Order, Order> getFunction() {
        return order -> {
            Assert.isTrue(!Arrays
                    .asList(OrderStatus.PAYMENT_SUCCEEDED, OrderStatus.PAYMENT_PENDING, OrderStatus.PAYMENT_FAILED)
                    .contains(order.getStatus()), "Payment has already been processed");
            Assert.isTrue(order.getStatus() == OrderStatus.PAYMENT_CONNECTED,
                    "Order must be in a payment connected state");

            // Get entity services
            OrderService orderService = order.getModule(OrderModule.class).getDefaultService();

            // Get the payment
            Payment payment = order.getPayment();

            // Update the order status
            order.setStatus(OrderStatus.PAYMENT_PENDING);
            order = orderService.update(order);

            try {
                // Create traverson for the new order
                Traverson traverson = new Traverson(URI.create(payment.getLink("self").getHref()), MediaTypes.HAL_JSON);
                payment = traverson.follow("commands", "processPayment").toObject(Payment.class);
            } catch (Exception ex) {
                log.error("The order's payment could not be processed", ex);

                OrderEvent event = new OrderEvent(OrderEventType.PAYMENT_FAILED, order);
                event.add(payment.getLink("self").withRel("payment"));

                // Trigger payment failed event
                order.sendAsyncEvent(event);

                throw ex;
            } finally {
                OrderEvent event = new OrderEvent(OrderEventType.PAYMENT_SUCCEEDED, order);
                event.add(payment.getLink("self").withRel("payment"));

                // Trigger payment succeeded event
                order.sendAsyncEvent(event);
            }

            return order;
        };
    }
}
