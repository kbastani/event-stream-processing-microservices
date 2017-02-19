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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Connects an {@link Order} to an Account.
 *
 * @author Kenny Bastani
 */
@Service
@Transactional
public class ConnectAccount extends Action<Order> {
    private final Logger log = Logger.getLogger(this.getClass());

    public Order apply(Order order, Long accountId) {
        Assert.isTrue(order.getStatus() == OrderStatus.ORDER_CREATED, "Order must be in a created state");

        OrderService orderService = order.getModule(OrderModule.class).getDefaultService();

        // Connect the account
        order.setAccountId(accountId);
        order.setStatus(OrderStatus.ACCOUNT_CONNECTED);
        order = orderService.update(order);

        try {
            // Trigger the account connected event
            order.sendAsyncEvent(new OrderEvent(OrderEventType.ACCOUNT_CONNECTED, order));
        } catch (Exception ex) {
            log.error("Could not connect order to account", ex);
            order.setAccountId(null);
            order.setStatus(OrderStatus.ORDER_CREATED);
            order = orderService.update(order);
        }

        return order;
    }

}
