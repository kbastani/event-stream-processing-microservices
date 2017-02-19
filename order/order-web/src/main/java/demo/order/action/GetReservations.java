package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
import demo.reservation.domain.ReservationModule;
import demo.reservation.domain.Reservations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Query action to get {@link demo.order.domain.Order}s for an an {@link Order}
 *
 * @author Kenny Bastani
 */
@Service
@Transactional
public class GetReservations extends Action<Order> {

    private final ReservationModule reservationModule;

    public GetReservations(ReservationModule reservationModule) {
        this.reservationModule = reservationModule;
    }

    public Reservations apply(Order order) {
        // Get orders from the order service
        return reservationModule.getDefaultService()
                .findReservationsByOrderId(order.getIdentity());
    }
}
