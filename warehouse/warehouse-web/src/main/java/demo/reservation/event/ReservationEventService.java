package demo.reservation.event;

import demo.event.BasicEventService;
import demo.event.EventSource;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.client.RestTemplate;

public class ReservationEventService extends BasicEventService<ReservationEvent, Long> {

    private final Source source;

    public ReservationEventService(ReservationEventRepository reservationEventRepository, EventSource eventSource,
            RestTemplate restTemplate, Source source) {
        super(reservationEventRepository, eventSource, restTemplate);
        this.source = source;
    }

    @Override
    public <S extends ReservationEvent> Boolean sendAsync(S event, Link... links) {
        // Send a duplicate event to the warehouse stream group output channel for data flow
        source.output()
                .send(MessageBuilder.withPayload(event)
                        .setHeader("contentType", MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .build());
        return super.sendAsync(event, links);
    }
}
