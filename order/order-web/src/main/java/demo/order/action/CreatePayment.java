package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
import demo.order.domain.OrderModule;
import demo.order.domain.OrderService;
import demo.order.domain.OrderStatus;
import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import demo.payment.domain.Payment;
import demo.payment.domain.PaymentMethod;
import demo.payment.domain.PaymentService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Creates a {@link Payment} for an {@link Order}.
 *
 * @author Kenny Bastani
 */
@Service
public class CreatePayment extends Action<Order> {

    private final Logger log = Logger.getLogger(this.getClass());
    private final PaymentService paymentService;

    public CreatePayment(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public Function<Order, Order> getFunction() {
        return order -> {
            Assert.isTrue(order.getPaymentId() == null, "Payment has already been created");
            Assert.isTrue(!Arrays.asList(OrderStatus.PAYMENT_CREATED,
                    OrderStatus.PAYMENT_CONNECTED,
                    OrderStatus.PAYMENT_SUCCEEDED,
                    OrderStatus.PAYMENT_PENDING).contains(order.getStatus()), "Payment has already been created");
            Assert.isTrue(order.getStatus() == OrderStatus.ACCOUNT_CONNECTED, "Account must be connected first");

            // Get entity services
            OrderService orderService = order.getModule(OrderModule.class).getDefaultService();

            // Update the order status
            order.setStatus(OrderStatus.PAYMENT_PENDING);
            order = orderService.update(order);

            Payment payment = new Payment();
            payment.setAmount(order.calculateTotal());
            payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
            payment = paymentService.create(payment);

            // Update the order status
            order.setStatus(OrderStatus.PAYMENT_CREATED);
            order = orderService.update(order);

            try {
                OrderEvent event = new OrderEvent(OrderEventType.PAYMENT_CREATED, order);
                event.add(payment.getLink("self").withRel("payment"));

                // Trigger payment created event
                order.sendAsyncEvent(event);
            } catch (Exception ex) {
                log.error("The order's payment could not be created", ex);

                // Rollback the payment creation
                if (payment.getIdentity() != null)
                    paymentService.delete(payment.getIdentity());

                order.setPaymentId(null);
                order.setStatus(OrderStatus.ACCOUNT_CONNECTED);
                orderService.update(order);

                throw ex;
            }

            return order;
        };
    }
}
