package demo.event;

import demo.account.AccountEventStatus;
import demo.account.AccountEventType;
import demo.account.AccountEvents;
import demo.state.StateMachineService;
import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link AccountEventStream} monitors for a variety of {@link AccountEvent} domain
 * events for an {@link demo.account.Account}.
 */
@EnableAutoConfiguration
@EnableBinding(Sink.class)
public class AccountEventStream {

    final private Logger logger = Logger.getLogger(AccountEventStream.class);
    final private StateMachineService stateMachineService;

    public AccountEventStream(StateMachineService stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    /**
     * Listen for a stream of {@link AccountEvent}
     *
     * @param accountEvent
     * @throws URISyntaxException
     */
    @StreamListener(Sink.INPUT)
    public void streamAccountEvents(AccountEvent accountEvent) throws URISyntaxException {
        logger.info("Account event received: " + accountEvent.toString());

        // Create a new ephemeral account state machine
        StateMachine<AccountEventStatus, AccountEventType> stateMachine =
                stateMachineService.getStateMachine();

        // Traverse the hypermedia link for the attached account
        Traverson traverson = new Traverson(
                new URI(accountEvent.getLink("account").getHref()),
                MediaTypes.HAL_JSON
        );

        // Traverse the hypermedia link to retrieve the event log for the account
        AccountEvents events = traverson.follow("events")
                .toEntity(AccountEvents.class)
                .getBody();

        // Create message headers
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("event", accountEvent);

        // Replicate the current state of the domain aggregate using state machine transitions
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
                    stateMachine.sendEvent(MessageBuilder
                            .createMessage(e.getType(), headers));
                });

        // Destroy the state machine
        stateMachine.stop();
    }
}
