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
import org.springframework.statemachine.StateMachine;

import java.net.URI;
import java.net.URISyntaxException;

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

    @StreamListener(Sink.INPUT)
    public void receive(AccountEvent accountEvent) throws URISyntaxException {
        logger.info(accountEvent);
        StateMachine<AccountEventStatus, AccountEventType> stateMachine =
                stateMachineService.getStateMachine();

        Traverson traverson = new Traverson(
                new URI(accountEvent.getLink("account").getHref()),
                MediaTypes.HAL_JSON
        );

        AccountEvents events = traverson.follow("events")
                        .toEntity(AccountEvents.class).getBody();


        events.getContent()
                .stream()
                .sorted((a1, a2) -> a1.getCreatedAt().compareTo(a2.getCreatedAt()))
                .forEach(e -> stateMachine.sendEvent(e.getType()));

        stateMachine.stop();
    }
}
