package demo.order.action;

import demo.domain.Action;
import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import demo.order.domain.Order;
import demo.order.domain.OrderModule;
import demo.order.domain.OrderService;
import demo.order.domain.OrderStatus;
import demo.payment.domain.Payment;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

/**
 * Connects a {@link Payment} to an {@link Order}.
 *
 * @author Kenny Bastani
 */
@Service
public class ConnectPayment extends Action<Order> {
    public BiConsumer<Order, Long> getConsumer() {
        return (order, paymentId) -> {

            OrderService orderService = order.getProvider(OrderModule.class)
                    .getDefaultService();

            // Connect the account
            order.setPaymentId(paymentId);
            order.setStatus(OrderStatus.PAYMENT_CONNECTED);
            order = orderService.update(order);

            // Trigger the account connected event
            order.sendAsyncEvent(new OrderEvent(OrderEventType.PAYMENT_CONNECTED, order));
        };
    }
}
