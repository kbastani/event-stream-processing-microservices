package demo.reservation.event;

import demo.event.EventRepository;

public interface ReservationEventRepository extends EventRepository<ReservationEvent, Long> {
}
