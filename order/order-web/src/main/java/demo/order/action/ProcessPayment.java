package demo.order.action;

import demo.domain.Action;
import demo.order.Order;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * Processes a {@link demo.payment.Payment} for an {@link Order}.
 *
 * @author Kenny Bastani
 */
@Service
public class ProcessPayment extends Action<Order> {
    public Consumer<Order> getConsumer() {
        return (order) -> {};
    }
}
