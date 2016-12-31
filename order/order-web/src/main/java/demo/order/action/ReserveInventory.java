package demo.order.action;

import demo.domain.Action;
import demo.order.Order;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class ReserveInventory extends Action<Order> {
    public Consumer<Order> getConsumer() {
        return (order) -> {};
    }
}
