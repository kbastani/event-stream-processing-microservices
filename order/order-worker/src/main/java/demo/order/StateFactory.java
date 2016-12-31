package demo.order;

import demo.order.domain.Order;
import demo.order.domain.OrderStatus;
import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import demo.order.event.OrderEvents;
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
public class StateFactory {

    final private Logger log = Logger.getLogger(StateFactory.class);
    final private StateService stateService;

    public StateFactory(StateService stateService) {
        this.stateService = stateService;
    }

    public Order apply(OrderEvent orderEvent) {
        Assert.notNull(orderEvent, "Cannot apply a null event");
        Assert.notNull(orderEvent.getId(), "The event payload's identity link was not found");

        StateMachine<OrderStatus, OrderEventType> stateMachine = getStateMachine(orderEvent);
        stateMachine.stop();

        return stateMachine.getExtendedState().get("order", Order.class);
    }

    private StateMachine<OrderStatus, OrderEventType> getStateMachine(OrderEvent orderEvent) {
        Link eventId = orderEvent.getId();
        log.info(String.format("Order event received: %s", eventId));

        StateMachine<OrderStatus, OrderEventType> stateMachine;
        Map<String, Object> contextMap;
        OrderEvents eventLog;

        eventLog = getEventLog(orderEvent);
        contextMap = getEventHeaders(orderEvent);
        stateMachine = stateService.newStateMachine();

        // Replicate the aggregate state
        eventLog.getContent().stream()
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .forEach(e -> stateMachine.sendEvent(MessageBuilder.createMessage(e.getType(), e.getId()
                        .equals(eventId) ? new MessageHeaders(contextMap) : new MessageHeaders(null))));

        return stateMachine;
    }

    private Map<String, Object> getEventHeaders(OrderEvent orderEvent) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("event", orderEvent);
        return headerMap;
    }

    private OrderEvents getEventLog(OrderEvent event) {
        // Follow the hypermedia link to fetch the attached order
        Traverson traverson = new Traverson(
                URI.create(event.getLink("order")
                        .getHref()),
                MediaTypes.HAL_JSON
        );

        // Get the event log for the attached order resource
        return traverson.follow("events")
                .toEntity(OrderEvents.class)
                .getBody();
    }
}
