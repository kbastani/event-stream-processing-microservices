package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
import demo.order.domain.OrderModule;
import demo.order.domain.OrderService;
import demo.order.domain.OrderStatus;
import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import demo.warehouse.domain.Warehouse;
import demo.warehouse.domain.WarehouseService;
import demo.warehouse.exception.WarehouseNotFoundException;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Arrays;

/**
 * Reserves inventory for an {@link Order}.
 *
 * @author Kenny Bastani
 */
@Service
@Transactional
public class ReserveInventory extends Action<Order> {

    private final Logger log = Logger.getLogger(ReserveInventory.class);
    private final WarehouseService warehouseService;

    public ReserveInventory(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }

    public Order apply(Order order) {
        Assert.isTrue(!Arrays
                .asList(OrderStatus.PAYMENT_SUCCEEDED, OrderStatus.PAYMENT_PENDING,
                        OrderStatus.PAYMENT_FAILED, OrderStatus.INVENTORY_RESERVED,
                        OrderStatus.RESERVATION_SUCCEEDED, OrderStatus.RESERVATION_PENDING,
                        OrderStatus.RESERVATION_FAILED)
                .contains(order.getStatus()), "Inventory has already been reserved");
        Assert.isTrue(order
                .getStatus() == OrderStatus.ACCOUNT_CONNECTED, "The order must be connected to an account");

        Warehouse warehouse;

        OrderService orderService = order.getModule(OrderModule.class).getDefaultService();

        OrderStatus status = order.getStatus();
        order.setStatus(OrderStatus.RESERVATION_PENDING);
        order = orderService.update(order);

        try {
            warehouse = warehouseService.findWarehouseWithInventory(order);
        } catch (WarehouseNotFoundException ex) {
            log.error("The order contains items that are not available at any warehouse", ex);
            throw ex;
        } catch (RuntimeException ex) {
            log.error("Error connecting to warehouse service", ex);
            // Rollback status change
            order.setStatus(status);
            order = orderService.update(order);
            throw ex;
        }

        try {
            // Reserve inventory for the order from the returned warehouse
            warehouse = warehouseService.reserveInventory(warehouse, order);
        } catch (Exception ex) {
            log.error("Could not reserve inventory for the order", ex);

            order.setStatus(OrderStatus.ACCOUNT_CONNECTED);
            order = orderService.update(order);

            OrderEvent event = new OrderEvent(OrderEventType.RESERVATION_FAILED, order);
            event.add(warehouse.getLink("self").withRel("warehouse"));

            // Trigger reservation failed
            order.sendAsyncEvent(event);
        } finally {
            if(order.getStatus() != OrderStatus.ACCOUNT_CONNECTED) {
                OrderEvent event = new OrderEvent(OrderEventType.RESERVATION_PENDING, order);
                event.add(warehouse.getLink("self").withRel("warehouse"));

                // Trigger reservation pending event
                order.sendAsyncEvent(event);
            }
        }

        return order;
    }
}
