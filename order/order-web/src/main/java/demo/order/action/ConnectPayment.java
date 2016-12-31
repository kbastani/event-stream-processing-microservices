package demo.order.action;

import demo.domain.Action;
import demo.event.OrderEvent;
import demo.event.OrderEventType;
import demo.order.Order;
import demo.order.OrderProvider;
import demo.order.OrderService;
import demo.order.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

/**
 * Connects a {@link demo.payment.Payment} to an {@link Order}.
 *
 * @author Kenny Bastani
 */
@Service
public class ConnectPayment extends Action<Order> {
    public BiConsumer<Order, Long> getConsumer() {
        return (order, paymentId) -> {

            OrderService orderService = order.getProvider(OrderProvider.class)
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
