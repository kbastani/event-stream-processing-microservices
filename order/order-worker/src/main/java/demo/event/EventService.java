package demo.event;

import demo.order.Order;
import demo.order.OrderStatus;
import demo.state.StateMachineService;
import org.apache.log4j.Logger;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
public class EventService {

    final private Logger log = Logger.getLogger(EventService.class);
    final private StateMachineService stateMachineService;

    public EventService(StateMachineService stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    public Order apply(OrderEvent orderEvent) {

        Order result;

        log.info("Order event received: " + orderEvent.getLink("self").getHref());

        // Generate a state machine for computing the state of the order resource
        StateMachine<OrderStatus, OrderEventType> stateMachine =
                stateMachineService.getStateMachine();

        // Follow the hypermedia link to fetch the attached order
        Traverson traverson = new Traverson(
                URI.create(orderEvent.getLink("order").getHref()),
                MediaTypes.HAL_JSON
        );

        // Get the event log for the attached order resource
        OrderEvents events = traverson.follow("events")
                .toEntity(OrderEvents.class)
                .getBody();

        // Prepare order event message headers
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("event", orderEvent);

        // Replicate the current state of the order resource
        events.getContent()
                .stream()
                .sorted((a1, a2) -> a1.getCreatedAt().compareTo(a2.getCreatedAt()))
                .forEach(e -> {
                    MessageHeaders headers = new MessageHeaders(null);

                    // Check to see if this is the current event
                    if (e.getLink("self").equals(orderEvent.getLink("self"))) {
                        headers = new MessageHeaders(headerMap);
                    }

                    // Send the event to the state machine
                    stateMachine.sendEvent(MessageBuilder.createMessage(e.getType(), headers));
                });


        // Get result
        Map<Object, Object> context = stateMachine.getExtendedState()
                .getVariables();

        // Get the order result
        result = (Order) context.getOrDefault("order", null);

        // Destroy the state machine
        stateMachine.stop();

        return result;
    }
}
