package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
import demo.order.domain.OrderModule;
import demo.order.domain.OrderService;
import demo.order.domain.OrderStatus;
import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.function.BiFunction;

/**
 * Connects an {@link Order} to an Account.
 *
 * @author Kenny Bastani
 */
@Service
public class ConnectAccount extends Action<Order> {
    private final Logger log = Logger.getLogger(this.getClass());

    public BiFunction<Order, Long, Order> getFunction() {
        return (order, accountId) -> {
            Assert.isTrue(order.getStatus() == OrderStatus.ORDER_CREATED, "Order must be in a created state");

            Order result;
            OrderService orderService = order.getModule(OrderModule.class).getDefaultService();

            // Connect the account
            order.setAccountId(accountId);
            order.setStatus(OrderStatus.ACCOUNT_CONNECTED);
            order = orderService.update(order);

            try {
                // Trigger the account connected event
                result = order.sendEvent(new OrderEvent(OrderEventType.ACCOUNT_CONNECTED, order)).getEntity();
            } catch (Exception ex) {
                log.error("Could not connect order to account", ex);
                order.setAccountId(null);
                order.setStatus(OrderStatus.ORDER_CREATED);
                orderService.update(order);
                throw new IllegalStateException("Could not connect order to account", ex);
            }

            return result;
        };
    }

}
