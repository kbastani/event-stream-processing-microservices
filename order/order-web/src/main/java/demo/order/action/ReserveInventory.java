package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * Reserves inventory for an {@link Order}.
 *
 * @author Kenny Bastani
 */
@Service
public class ReserveInventory extends Action<Order> {
    public Consumer<Order> getConsumer() {
        return (order) -> {};
    }
}
