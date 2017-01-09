package demo.event;

import demo.account.Account;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Profile;
import org.springframework.statemachine.StateMachine;

/**
 * The {@link AccountEventStream} monitors for a variety of {@link AccountEvent} domain
 * events for an {@link Account}.
 *
 * @author kbastani
 */
@EnableAutoConfiguration
@EnableBinding(Sink.class)
@Profile({ "cloud", "development" })
public class AccountEventStream {

    private EventService eventService;

    public AccountEventStream(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Listens to a stream of incoming {@link AccountEvent} messages. For each
     * new message received, replicate an in-memory {@link StateMachine} that
     * reproduces the current state of the {@link Account} resource that is the
     * subject of the {@link AccountEvent}.
     *
     * @param accountEvent is the {@link Account} domain event to process
     */
    @StreamListener(Sink.INPUT)
    public void streamListener(AccountEvent accountEvent) {
        eventService.apply(accountEvent);
    }
}
