package demo.event;

import demo.payment.Payment;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Profile;
import org.springframework.statemachine.StateMachine;

/**
 * The {@link PaymentEventStream} monitors for a variety of {@link PaymentEvent} domain
 * events for an {@link Payment}.
 *
 * @author kbastani
 */
@EnableAutoConfiguration
@EnableBinding(Sink.class)
@Profile({ "cloud", "development" })
public class PaymentEventStream {

    private EventService eventService;

    public PaymentEventStream(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Listens to a stream of incoming {@link PaymentEvent} messages. For each
     * new message received, replicate an in-memory {@link StateMachine} that
     * reproduces the current state of the {@link Payment} resource that is the
     * subject of the {@link PaymentEvent}.
     *
     * @param paymentEvent is the {@link Payment} domain event to process
     */
    @StreamListener(Sink.INPUT)
    public void streamListerner(PaymentEvent paymentEvent) {
        eventService.apply(paymentEvent);
    }
}
