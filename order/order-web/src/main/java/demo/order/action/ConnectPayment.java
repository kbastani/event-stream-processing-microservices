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
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.function.BiFunction;

/**
 * Connects a {@link Payment} to an {@link Order}.
 *
 * @author Kenny Bastani
 */
@Service
public class ConnectPayment extends Action<Order> {
    private final Logger log = Logger.getLogger(this.getClass());

    public BiFunction<Order, Long, Order> getFunction() {
        return (order, paymentId) -> {
            Assert.isTrue(order
                    .getStatus() == OrderStatus.PAYMENT_CREATED, "Order must be in a payment created state");

            OrderService orderService = order.getModule(OrderModule.class).getDefaultService();

            // Connect the payment
            order.setPaymentId(paymentId);
            order.setStatus(OrderStatus.PAYMENT_CONNECTED);
            order = orderService.update(order);

            try {
                // Trigger the payment connected event
                order.sendAsyncEvent(new OrderEvent(OrderEventType.PAYMENT_CONNECTED, order));
            } catch (Exception ex) {
                log.error("Could not connect payment to order", ex);
                order.setPaymentId(null);
                order.setStatus(OrderStatus.ORDER_CREATED);
                orderService.update(order);
                throw ex;
            }

            return order;
        };
    }
}
