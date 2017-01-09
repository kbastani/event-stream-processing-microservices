package demo.reservation.event;

import demo.event.BasicEventService;
import demo.reservation.config.ReservationEventSource;
import org.springframework.web.client.RestTemplate;

public class ReservationEventService extends BasicEventService<ReservationEvent, Long> {
    public ReservationEventService(ReservationEventRepository eventRepository, ReservationEventSource eventStream,
            RestTemplate restTemplate) {
        super(eventRepository, eventStream, restTemplate);
    }
}
