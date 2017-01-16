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

import java.util.Arrays;

/**
 * Completes the {@link Order} and applies a final status
 *
 * @author Kenny Bastani
 */
@Service
@Transactional
public class CompleteOrder extends Action<Order> {

    private final Logger log = Logger.getLogger(CompleteOrder.class);

    public Order apply(Order order) {
        Assert.isTrue(Arrays.asList(OrderStatus.PAYMENT_FAILED,
                OrderStatus.PAYMENT_SUCCEEDED,
                OrderStatus.RESERVATION_FAILED).contains(order.getStatus()), "Order must be in a terminal state");

        OrderService orderService = order.getModule(OrderModule.class).getDefaultService();

        OrderStatus status = order.getStatus();

        try {
            if (order.getStatus() == OrderStatus.PAYMENT_SUCCEEDED) {
                order.setStatus(OrderStatus.ORDER_SUCCEEDED);
                order = orderService.update(order);
                order.sendAsyncEvent(new OrderEvent(OrderEventType.ORDER_SUCCEEDED, order));
            } else {
                order.setStatus(OrderStatus.ORDER_FAILED);
                order = orderService.update(order);
                order.sendAsyncEvent(new OrderEvent(OrderEventType.ORDER_FAILED, order));
            }
        } catch (RuntimeException ex) {
            log.error("Error completing the order", ex);
            // Rollback status change
            order.setStatus(status);
            order = orderService.update(order);
        }

        return order;
    }
}
