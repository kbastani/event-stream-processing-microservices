package demo.event;

import demo.account.Account;
import demo.account.AccountController;
import demo.account.AccountEventType;
import demo.log.Log;
import demo.log.LogRepository;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
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
 */
@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final LogRepository logRepository;
    private final RepositoryEntityLinks entityLinks;
    private final ProducerChannels producer;

    public EventService(EventRepository eventRepository, LogRepository logRepository,
                        RepositoryEntityLinks entityLinks, ProducerChannels producerChannels) {
        this.eventRepository = eventRepository;
        this.logRepository = logRepository;
        this.entityLinks = entityLinks;
        this.producer = producerChannels;
    }

    public Resource<AccountEvent> createEvent(AccountEvent event) {
        Resource<AccountEvent> eventResource = null;

        // Save new event
        event = addEvent(event);

        if (event != null) {
            // Create account event resource
            eventResource = new Resource<>(event, Arrays.asList(
                    entityLinks.linkFor(AccountEvent.class, event.getId())
                            .slash(event.getId())
                            .withRel("self"),
                    entityLinks.linkFor(AccountEvent.class)
                            .slash(event.getId())
                            .slash("logs")
                            .withRel("logs"),
                    linkTo(AccountController.class)
                            .slash("accounts")
                            .slash(event.getAccount().getId())
                            .withRel("account"))
            );

            // Produce account event
            producer.output()
                    .send(MessageBuilder
                            .withPayload(eventResource)
                            .build());
        }

        return eventResource;
    }

    private AccountEvent addEvent(AccountEvent event) {
        event = eventRepository.save(event);
        return event;
    }

    public AccountEvent getEvent(Long id) {
        return eventRepository.findOne(id);
    }

    public AccountEvent updateEvent(Long id, AccountEvent event) {
        Assert.notNull(id);
        Assert.isTrue(event.getId() == null || Objects.equals(id, event.getId()));

        return eventRepository.save(event);
    }

    public Resource<Log> appendEventLog(Long eventId, Log log) {
        Assert.notNull(eventId);
        Assert.notNull(log);

        Resource<Log> logResource = null;
        AccountEvent event = getEvent(eventId);

        if (event != null) {
            log = logRepository.save(log);
            event.getLogs().add(log);

            logResource = new Resource<>(log, Arrays.asList(
                    entityLinks.linkFor(Log.class)
                            .slash(log.getId())
                            .withSelfRel(),
                    entityLinks.linkFor(AccountEvent.class)
                            .slash(event.getId())
                            .withRel("event")
            ));
        }

        return logResource;
    }
}
