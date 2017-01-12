package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
import demo.order.domain.OrderModule;
import demo.order.domain.OrderService;
import demo.order.domain.OrderStatus;
import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import org.apache.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.function.BiFunction;

/**
 * Connects an {@link Order} to an Account.
 *
 * @author Kenny Bastani
 */
@Service
public class AddReservation extends Action<Order> {
    private final Logger log = Logger.getLogger(this.getClass());

    public BiFunction<Order, Long, Order> getFunction() {
        return (order, reservationId) -> {
            Assert.isTrue(order
                    .getStatus() == OrderStatus.RESERVATION_PENDING, "Order must be in a pending reservation state");
            Assert.isTrue(!order.getReservationIds().contains(reservationId), "Reservation already added to order");

            OrderService orderService = order.getModule(OrderModule.class).getDefaultService();

            order.getReservationIds().add(reservationId);
            order = orderService.update(order);

            Link reservationLink = new Link(new UriTemplate("http://warehouse-web/v1/reservations/{id}")
                    .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                    .expand(reservationId)
                    .toString()).withRel("reservation");

            try {
                // Trigger reservation added event
                order.sendAsyncEvent(new OrderEvent(OrderEventType.RESERVATION_ADDED, order), reservationLink);
            } catch (Exception ex) {
                log.error("Could not add reservation to order", ex);
                order.getReservationIds().remove(reservationId);
                order.setStatus(OrderStatus.RESERVATION_FAILED);
                orderService.update(order);
                order.sendAsyncEvent(new OrderEvent(OrderEventType.RESERVATION_FAILED, order), reservationLink);
                throw ex;
            }

            return order;
        };
    }

}
