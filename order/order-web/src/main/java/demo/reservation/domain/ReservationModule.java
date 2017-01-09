package demo.reservation.domain;

import demo.domain.Module;
import demo.event.EventService;
import demo.order.event.OrderEvent;

@org.springframework.stereotype.Service
public class ReservationModule extends Module<Reservation> {

    private final ReservationService reservationService;
    private final EventService<OrderEvent, Long> eventService;

    public ReservationModule(ReservationService reservationService, EventService<OrderEvent, Long> eventService) {
        this.reservationService = reservationService;
        this.eventService = eventService;
    }

    public ReservationService getReservationService() {
        return reservationService;
    }

    public EventService<OrderEvent, Long> getEventService() {
        return eventService;
    }

    @Override
    public ReservationService getDefaultService() {
        return reservationService;
    }

    @Override
    public EventService<OrderEvent, Long> getDefaultEventService() {
        return eventService;
    }
}
