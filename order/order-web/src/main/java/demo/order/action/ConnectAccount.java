package demo.order.action;

import demo.domain.Action;
import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import demo.order.domain.Order;
import demo.order.domain.OrderModule;
import demo.order.domain.OrderService;
import demo.order.domain.OrderStatus;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

/**
 * Connects an {@link Order} to an Account.
 *
 * @author Kenny Bastani
 */
@Service
public class ConnectAccount extends Action<Order> {

    public BiConsumer<Order, Long> getConsumer() {
        return (order, accountId) -> {
            OrderService orderService = order.getProvider(OrderModule.class)
                    .getDefaultService();

            // Connect the account
            order.setAccountId(accountId);
            order.setStatus(OrderStatus.ACCOUNT_CONNECTED);
            order = orderService.update(order);

            // Trigger the account connected event
            order.sendAsyncEvent(new OrderEvent(OrderEventType.ACCOUNT_CONNECTED, order));
        };
    }
}
