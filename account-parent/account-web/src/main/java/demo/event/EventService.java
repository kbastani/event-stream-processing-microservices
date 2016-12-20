package demo.event;

import demo.account.Account;
import demo.account.AccountController;
import org.apache.log4j.Logger;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.RequestEntity;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * The {@link EventService} provides transactional service methods for {@link AccountEvent}
 * entities of the Account Service. Account domain events are generated with a {@link AccountEventType},
 * and action logs are appended to the {@link AccountEvent}.
 *
 * @author kbastani
 */
@Service
@CacheConfig(cacheNames = {"events"})
public class EventService {

    private final Logger log = Logger.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final Source accountStreamSource;
    private final RestTemplate restTemplate;

    public EventService(EventRepository eventRepository, Source accountStreamSource, RestTemplate restTemplate) {
        this.eventRepository = eventRepository;
        this.accountStreamSource = accountStreamSource;
        this.restTemplate = restTemplate;
    }

    /**
     * Create a new {@link AccountEvent} and append it to the event log of the referenced {@link Account}.
     * After the {@link AccountEvent} has been persisted, send the event to the account stream. Events can
     * be raised as a blocking or non-blocking operation depending on the {@link ConsistencyModel}.
     *
     * @param accountId        is the unique identifier for the {@link Account}
     * @param event            is the {@link AccountEvent} to create
     * @param consistencyModel is the desired consistency model for the response
     * @return an {@link AccountEvent} that has been appended to the {@link Account}'s event log
     */
    public AccountEvent createEvent(Long accountId, AccountEvent event, ConsistencyModel consistencyModel) {
        event = createEvent(accountId, event);
        return raiseEvent(event, consistencyModel);
    }

    /**
     * Raise an {@link AccountEvent} that attempts to transition the state of an {@link Account}.
     *
     * @param event            is an {@link AccountEvent} that will be raised
     * @param consistencyModel is the consistency model for this request
     * @return an {@link AccountEvent} that has been appended to the {@link Account}'s event log
     */
    public AccountEvent raiseEvent(AccountEvent event, ConsistencyModel consistencyModel) {
        switch (consistencyModel) {
            case BASE:
                asyncRaiseEvent(event);
                break;
            case ACID:
                event = raiseEvent(event);
                break;
        }

        return event;
    }

    /**
     * Raise an asynchronous {@link AccountEvent} by sending an AMQP message to the account stream. Any
     * state changes will be applied to the {@link Account} outside of the current HTTP request context.
     * <p>
     * Use this operation when a workflow can be processed asynchronously outside of the current HTTP
     * request context.
     *
     * @param event is an {@link AccountEvent} that will be raised
     */
    private void asyncRaiseEvent(AccountEvent event) {
        // Append the account event to the stream
        accountStreamSource.output()
                .send(MessageBuilder
                        .withPayload(getAccountEventResource(event))
                        .build());
    }

    /**
     * Raise a synchronous {@link AccountEvent} by sending a HTTP request to the account stream. The response
     * is a blocking operation, which ensures that the result of a multi-step workflow will not return until
     * the transaction reaches a consistent state.
     * <p>
     * Use this operation when the result of a workflow must be returned within the current HTTP request context.
     *
     * @param event is an {@link AccountEvent} that will be raised
     * @return an {@link AccountEvent} which contains the consistent state of an {@link Account}
     */
    private AccountEvent raiseEvent(AccountEvent event) {
        try {
            // Create a new request entity
            RequestEntity<Resource<AccountEvent>> requestEntity = RequestEntity.post(
                    URI.create("http://localhost:8081/v1/events"))
                    .contentType(MediaTypes.HAL_JSON)
                    .body(getAccountEventResource(event), Resource.class);

            // Update the account entity's status
            Account result = restTemplate.exchange(requestEntity, Account.class)
                    .getBody();

            log.info(result);
            event.setAccount(result);
        } catch (Exception ex) {
            log.error(ex);
        }

        return event;
    }


    /**
     * Create a new {@link AccountEvent} and publish it to the account stream.
     *
     * @param event is the {@link AccountEvent} to publish to the account stream
     * @return a hypermedia {@link AccountEvent} resource
     */
    @CacheEvict(cacheNames = "events", key = "#id.toString()")
    public AccountEvent createEvent(Long id, AccountEvent event) {
        // Save new event
        event = addEvent(event);
        Assert.notNull(event, "The event could not be appended to the account");

        return event;
    }

    /**
     * Get an {@link AccountEvent} with the supplied identifier.
     *
     * @param id is the unique identifier for the {@link AccountEvent}
     * @return an {@link AccountEvent}
     */
    public Resource<AccountEvent> getEvent(Long id) {
        return getAccountEventResource(eventRepository.findOne(id));
    }

    /**
     * Update an {@link AccountEvent} with the supplied identifier.
     *
     * @param id    is the unique identifier for the {@link AccountEvent}
     * @param event is the {@link AccountEvent} to update
     * @return the updated {@link AccountEvent}
     */
    @CacheEvict(cacheNames = "events", key = "#event.getAccount().getAccountId().toString()")
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
    @Cacheable(cacheNames = "events", key = "#id.toString()")
    public List<AccountEvent> getAccountEvents(Long id) {
        return eventRepository.findAccountEventsByAccountId(id,
                new PageRequest(0, Integer.MAX_VALUE)).getContent();
    }

    /**
     * Gets a hypermedia resource for a {@link AccountEvent} entity.
     *
     * @param event is the {@link AccountEvent} to enrich with hypermedia
     * @return a hypermedia resource for the supplied {@link AccountEvent} entity
     */
    private Resource<AccountEvent> getAccountEventResource(AccountEvent event) {
        return new Resource<AccountEvent>(event, Arrays.asList(
                linkTo(AccountController.class)
                        .slash("events")
                        .slash(event.getEventId())
                        .withSelfRel(),
                linkTo(AccountController.class)
                        .slash("accounts")
                        .slash(event.getAccount().getAccountId())
                        .withRel("account")));
    }

    /**
     * Add a {@link AccountEvent} to an {@link Account} entity.
     *
     * @param event is the {@link AccountEvent} to append to an {@link Account} entity
     * @return the newly appended {@link AccountEvent} entity
     */
    @CacheEvict(cacheNames = "events", key = "#event.getAccount().getAccountId().toString()")
    private AccountEvent addEvent(AccountEvent event) {
        event = eventRepository.saveAndFlush(event);
        return event;
    }
}
