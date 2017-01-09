package demo.reservation.event;

import demo.domain.AbstractEntity;
import demo.reservation.domain.Reservation;

/**
 * The domain event {@link ReservationEvent} tracks the type and state of events as applied to the {@link Reservation} domain
 * object. This event resource can be used to event source the aggregate state of {@link Reservation}.
 *
 * @author kbastani
 */
public class ReservationEvent extends AbstractEntity {

    private ReservationEventType type;

    public ReservationEvent() {
    }

    public ReservationEvent(ReservationEventType type) {
        this.type = type;
    }

    public ReservationEventType getType() {
        return type;
    }

    public void setType(ReservationEventType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ReservationEvent{" +
                "type=" + type +
                "} " + super.toString();
    }
}
