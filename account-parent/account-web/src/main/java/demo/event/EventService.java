package demo.event;

import demo.account.Account;
import demo.account.AccountController;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.Resource;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Objects;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * The {@link EventService} provides transactional service methods for {@link AccountEvent}
 * entities of the Account Service. Account domain events are generated with a {@link AccountEventType},
 * and action logs are appended to the {@link AccountEvent}. The logs resource provides an append-only transaction
 * log that can be used to source the state of the {@link Account}
 *
 * @author kbastani
 */
@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final Source accountStreamSource;

    public EventService(EventRepository eventRepository, Source accountStreamSource) {
        this.eventRepository = eventRepository;
        this.accountStreamSource = accountStreamSource;
    }

    /**
     * Create a new {@link AccountEvent} and publish it to the account stream.
     *
     * @param event is the {@link AccountEvent} to publish to the account stream
     * @return a hypermedia {@link AccountEvent} resource
     */
    public AccountEvent createEvent(AccountEvent event) {
        // Save new event
        event = addEvent(event);
        Assert.notNull(event, "The event could not be appended to the account");

        // Append the account event to the stream
        accountStreamSource.output()
                .send(MessageBuilder
                        .withPayload(getAccountEventResource(event))
                        .build());

        return event;
    }

    /**
     * Get an {@link AccountEvent} with the supplied identifier.
     *
     * @param id is the unique identifier for the {@link AccountEvent}
     * @return an {@link AccountEvent}
     */
    public AccountEvent getEvent(Long id) {
        return eventRepository.findOne(id);
    }

    /**
     * Update an {@link AccountEvent} with the supplied identifier.
     *
     * @param id    is the unique identifier for the {@link AccountEvent}
     * @param event is the {@link AccountEvent} to update
     * @return the updated {@link AccountEvent}
     */
    public AccountEvent updateEvent(Long id, AccountEvent event) {
        Assert.notNull(id);
        Assert.isTrue(event.getId() == null || Objects.equals(id, event.getId()));

        return eventRepository.save(event);
    }

    /**
     * Get {@link AccountEvents} for the supplied {@link Account} identifier.
     *
     * @param id is the unique identifier of the {@link Account}
     * @return a list of {@link AccountEvent} wrapped in a hypermedia {@link AccountEvents} resource
     */
    public AccountEvents getEvents(Long id) {
        Page<AccountEvent> events = eventRepository.findAccountEventsByAccountId(id, new PageRequest(0, Integer.MAX_VALUE));
        return new AccountEvents(id, events);
    }

    /**
     * Gets a hypermedia resource for a {@link AccountEvent} entity.
     *
     * @param event is the {@link AccountEvent} to enrich with hypermedia
     * @return a hypermedia resource for the supplied {@link AccountEvent} entity
     */
    private Resource<AccountEvent> getAccountEventResource(AccountEvent event) {
        return new Resource<>(event, Arrays.asList(
                linkTo(AccountController.class)
                        .slash("events")
                        .slash(event.getEventId())
                        .withSelfRel(),
                linkTo(AccountController.class)
                        .slash("accounts")
                        .slash(event.getAccount().getAccountId())
                        .withRel("account"))
        );
    }

    /**
     * Add a {@link AccountEvent} to an {@link Account} entity.
     *
     * @param event is the {@link AccountEvent} to append to an {@link Account} entity
     * @return the newly appended {@link AccountEvent} entity
     */
    private AccountEvent addEvent(AccountEvent event) {
        event = eventRepository.save(event);
        return event;
    }
}
