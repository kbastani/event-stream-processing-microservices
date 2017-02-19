package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
import demo.order.domain.OrderModule;
import demo.order.domain.OrderService;
import demo.order.domain.OrderStatus;
import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import demo.reservation.domain.Reservation;
import demo.reservation.domain.ReservationStatus;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

import static demo.order.domain.OrderStatus.RESERVATION_FAILED;
import static demo.order.domain.OrderStatus.RESERVATION_SUCCEEDED;

/**
 * Reserves inventory for an {@link Order}.
 *
 * @author Kenny Bastani
 */
@Service
@Transactional
public class CompleteReservation extends Action<Order> {

    private final Logger log = Logger.getLogger(CompleteReservation.class);

    public Order apply(Order order) {
        if (order.getStatus() != RESERVATION_SUCCEEDED && order.getStatus() != RESERVATION_FAILED) {
            Assert.isTrue(order.getStatus() == OrderStatus.RESERVATION_PENDING,
                    "The order must be in a reservation pending state");
        } else {
            // Reservation has already completed
            return order;
        }

        OrderService orderService = order.getModule(OrderModule.class).getDefaultService();

        OrderStatus status = order.getStatus();

        try {
            List<Reservation> reservations = order.getReservations().getContent().stream()
                    .collect(Collectors.toList());

            // Check if all inventory has been reserved
            Boolean orderReserved = reservations.stream()
                    .allMatch(r -> r.getStatus() == ReservationStatus.RESERVATION_SUCCEEDED);

            // Check if any inventory reservations have failed
            Boolean reservationFailed = reservations.stream()
                    .anyMatch(r -> r.getStatus() == ReservationStatus.RESERVATION_FAILED);

            if (orderReserved && order.getStatus() == OrderStatus.RESERVATION_PENDING) {
                // Succeed the reservation and commit all inventory associated with order
                order.setStatus(RESERVATION_SUCCEEDED);
                order = orderService.update(order);
                order.sendAsyncEvent(new OrderEvent(OrderEventType.RESERVATION_SUCCEEDED, order));
            } else if (reservationFailed && order.getStatus() == OrderStatus.RESERVATION_PENDING) {
                // Fail the reservation and release all inventory associated with order
                order.setStatus(RESERVATION_FAILED);
                order = orderService.update(order);
                order.sendAsyncEvent(new OrderEvent(OrderEventType.RESERVATION_FAILED, order));
            }
        } catch (RuntimeException ex) {
            log.error("Error completing reservation", ex);
            // Rollback status change
            order.setStatus(status);
            order = orderService.update(order);
        }

        return order;
    }
}
