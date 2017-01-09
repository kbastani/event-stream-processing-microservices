package demo.reservation;

import demo.reservation.domain.Reservation;
import demo.reservation.domain.ReservationStatus;
import demo.reservation.event.ReservationEvent;
import demo.reservation.event.ReservationEventType;
import demo.reservation.event.ReservationEvents;
import org.apache.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReservationStateFactory {

    final private Logger log = Logger.getLogger(ReservationStateFactory.class);
    final private ReservationStateService stateService;

    public ReservationStateFactory(ReservationStateService stateService) {
        this.stateService = stateService;
    }

    public Reservation apply(ReservationEvent reservationEvent) {
        Assert.notNull(reservationEvent, "Cannot apply a null event");
        Assert.notNull(reservationEvent.getId(), "The event payload's identity link was not found");

        StateMachine<ReservationStatus, ReservationEventType> stateMachine = getStateMachine(reservationEvent);
        stateMachine.stop();

        return stateMachine.getExtendedState().get("reservation", Reservation.class);
    }

    private StateMachine<ReservationStatus, ReservationEventType> getStateMachine(ReservationEvent reservationEvent) {
        Link eventId = reservationEvent.getId();
        log.info(String.format("Reservation event received: %s", eventId));

        StateMachine<ReservationStatus, ReservationEventType> stateMachine;
        Map<String, Object> contextMap;
        ReservationEvents eventLog;

        eventLog = getEventLog(reservationEvent);
        contextMap = getEventHeaders(reservationEvent);
        stateMachine = stateService.newStateMachine();

        // Replicate the aggregate state
        eventLog.getContent().stream()
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .forEach(e -> stateMachine.sendEvent(MessageBuilder.createMessage(e.getType(), e.getId()
                        .equals(eventId) ? new MessageHeaders(contextMap) : new MessageHeaders(null))));

        return stateMachine;
    }

    private Map<String, Object> getEventHeaders(ReservationEvent reservationEvent) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("event", reservationEvent);
        return headerMap;
    }

    private ReservationEvents getEventLog(ReservationEvent event) {
        // Follow the hypermedia link to fetch the attached reservation
        Traverson traverson = new Traverson(
                URI.create(event.getLink("reservation")
                        .getHref()),
                MediaTypes.HAL_JSON
        );

        // Get the event log for the attached reservation resource
        return traverson.follow("events")
                .toEntity(ReservationEvents.class)
                .getBody();
    }
}
