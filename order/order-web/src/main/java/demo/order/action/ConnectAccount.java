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

@Service
public class ConnectAccount extends Action<Order> {

    public BiConsumer<Order, Long> getConsumer() {
        return (order, accountId) -> {
            OrderService orderService = order.getProvider(OrderProvider.class)
                    .getDefaultService();

            // Connect the account
            order.setAccountId(accountId);
            order.setStatus(OrderStatus.ACCOUNT_CONNECTED);
            order = orderService.update(order);

            // Trigger the account connected event
            order.sendAsyncEvent(new OrderEvent(OrderEventType.ACCOUNT_CONNECTED));
        };
    }
}
