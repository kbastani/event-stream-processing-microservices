package demo.event;

import demo.account.Account;
import demo.account.AccountStatus;
import demo.state.StateMachineService;
import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link AccountEventStream} monitors for a variety of {@link AccountEvent} domain
 * events for an {@link Account}.
 *
 * @author kbastani
 */
@EnableAutoConfiguration
@EnableBinding(Sink.class)
public class AccountEventStream {

    final private Logger log = Logger.getLogger(AccountEventStream.class);
    final private StateMachineService stateMachineService;

    public AccountEventStream(StateMachineService stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    /**
     * Listens to a stream of incoming {@link AccountEvent} messages. For each
     * new message received, replicate an in-memory {@link StateMachine} that
     * reproduces the current state of the {@link Account} resource that is the
     * subject of the {@link AccountEvent}.
     *
     * @param accountEvent
     */
    @StreamListener(Sink.INPUT)
    public void streamListerner(AccountEvent accountEvent) {
        log.info("Account event received: " + accountEvent.getLink("self").getHref());

        // Generate a state machine for computing the state of the account resource
        StateMachine<AccountStatus, AccountEventType> stateMachine =
                stateMachineService.getStateMachine();

        // Follow the hypermedia link to fetch the attached account
        Traverson traverson = new Traverson(
                URI.create(accountEvent.getLink("account").getHref()),
                MediaTypes.HAL_JSON
        );

        // Get the event log for the attached account resource
        AccountEvents events = traverson.follow("events")
                .toEntity(AccountEvents.class)
                .getBody();

        // Prepare account event message headers
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("event", accountEvent);

        // Replicate the current state of the account resource
        events.getContent()
                .stream()
                .sorted((a1, a2) -> a1.getCreatedAt().compareTo(a2.getCreatedAt()))
                .forEach(e -> {
                    MessageHeaders headers = new MessageHeaders(null);

                    // Check to see if this is the current event
                    if (e.getLink("self").equals(accountEvent.getLink("self"))) {
                        headers = new MessageHeaders(headerMap);
                    }

                    // Send the event to the state machine
                    stateMachine.sendEvent(MessageBuilder.createMessage(e.getType(), headers));
                });

        // Destroy the state machine
        stateMachine.stop();
    }
}
