package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
import demo.order.domain.OrderService;
import demo.order.domain.OrderStatus;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

/**
 * Updates the status of a {@link Order} entity.
 *
 * @author Kenny Bastani
 */
@Service
public class UpdateOrderStatus extends Action<Order> {
    private final Logger log = Logger.getLogger(this.getClass());

    private final OrderService orderService;

    public UpdateOrderStatus(OrderService orderService) {
        this.orderService = orderService;
    }

    public BiFunction<Order, OrderStatus, Order> getFunction() {
        return (order, orderStatus) -> {

            // Save rollback status
            OrderStatus rollbackStatus = order.getStatus();

            try {
                // Update status
                order.setStatus(orderStatus);
                order = orderService.update(order);
            } catch (Exception ex) {
                log.error("Could not update the status", ex);
                order.setStatus(rollbackStatus);
                order = orderService.update(order);
                throw ex;
            }

            return order;
        };
    }
}
