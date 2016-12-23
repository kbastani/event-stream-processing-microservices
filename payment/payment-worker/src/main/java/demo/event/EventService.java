package demo.event;

import demo.payment.Payment;
import demo.payment.PaymentStatus;
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

    public Payment apply(PaymentEvent paymentEvent) {

        Payment result;

        log.info("Payment event received: " + paymentEvent.getLink("self").getHref());

        // Generate a state machine for computing the state of the payment resource
        StateMachine<PaymentStatus, PaymentEventType> stateMachine =
                stateMachineService.getStateMachine();

        // Follow the hypermedia link to fetch the attached payment
        Traverson traverson = new Traverson(
                URI.create(paymentEvent.getLink("payment").getHref()),
                MediaTypes.HAL_JSON
        );

        // Get the event log for the attached payment resource
        PaymentEvents events = traverson.follow("events")
                .toEntity(PaymentEvents.class)
                .getBody();

        // Prepare payment event message headers
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("event", paymentEvent);

        // Replicate the current state of the payment resource
        events.getContent()
                .stream()
                .sorted((a1, a2) -> a1.getCreatedAt().compareTo(a2.getCreatedAt()))
                .forEach(e -> {
                    MessageHeaders headers = new MessageHeaders(null);

                    // Check to see if this is the current event
                    if (e.getLink("self").equals(paymentEvent.getLink("self"))) {
                        headers = new MessageHeaders(headerMap);
                    }

                    // Send the event to the state machine
                    stateMachine.sendEvent(MessageBuilder.createMessage(e.getType(), headers));
                });


        // Get result
        Map<Object, Object> context = stateMachine.getExtendedState()
                .getVariables();

        // Get the payment result
        result = (Payment) context.getOrDefault("payment", null);

        // Destroy the state machine
        stateMachine.stop();

        return result;
    }
}
