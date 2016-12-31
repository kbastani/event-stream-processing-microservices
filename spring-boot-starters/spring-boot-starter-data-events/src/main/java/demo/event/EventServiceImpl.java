package demo.event;

import demo.domain.Aggregate;
import org.apache.log4j.Logger;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.client.RestTemplate;

import java.io.Serializable;
import java.net.URI;

/**
 * Event service implementation of {@link EventService} for managing {@link Event} entities.
 *
 * @author Kenny Bastani
 * @see Event
 * @see Events
 * @see EventService
 */
@SuppressWarnings("unchecked")
class EventServiceImpl<T extends Event, ID extends Serializable> implements EventService<T, ID> {

    private static final Logger log = Logger.getLogger(EventServiceImpl.class);
    private static final String EVENT_PROCESSOR_URL = "http://localhost:8083/v1/events";

    private final EventRepository<T, ID> eventRepository;
    private final Source eventStream;
    private final RestTemplate restTemplate;

    EventServiceImpl(EventRepository<T, ID> eventRepository, Source eventStream, RestTemplate restTemplate) {
        this.eventRepository = eventRepository;
        this.eventStream = eventStream;
        this.restTemplate = restTemplate;
    }

    public <E extends Aggregate, S extends T> S send(S event, Link... links) {
        // Assemble request to the event stream processor
        RequestEntity<Resource<T>> requestEntity = RequestEntity.post(URI.create(EVENT_PROCESSOR_URL))
                .contentType(MediaTypes.HAL_JSON)
                .body(new Resource<T>(event), Resource.class);

        try {
            // Send the event to the event stream processor
            E entity = (E) restTemplate.exchange(requestEntity, event.getEntity()
                    .getClass())
                    .getBody();

            // Set the applied entity reference to the event
            event.setEntity(entity);
        } catch (Exception ex) {
            log.error(ex);
        }

        return event;
    }

    public <S extends T> Boolean sendAsync(S event, Link... links) {
        return eventStream.output()
                .send(MessageBuilder.withPayload(event)
                        .setHeader("contentType", MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .build());
    }

    public <S extends T> S save(S event) {
        event = eventRepository.save(event);
        return event;
    }

    public <S extends T> S save(ID id, S event) {
        event.setEventId(id);
        return save(event);
    }

    public <S extends ID> T findOne(S id) {
        return eventRepository.findOne(id);
    }

    public <E extends Events> E find(ID entityId) {
        return (E) new Events(entityId, eventRepository.findEventsByEntityId(entityId,
                new PageRequest(0, Integer.MAX_VALUE))
                .getContent());
    }
}
