package demo.event;

import demo.payment.Payment;
import demo.payment.PaymentController;
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
 * The {@link EventService} provides transactional service methods for {@link PaymentEvent}
 * entities of the Payment Service. Payment domain events are generated with a {@link PaymentEventType},
 * and action logs are appended to the {@link PaymentEvent}.
 *
 * @author kbastani
 */
@Service
@CacheConfig(cacheNames = {"payment-events"})
public class EventService {

    private final Logger log = Logger.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final Source paymentStreamSource;
    private final RestTemplate restTemplate;

    public EventService(EventRepository eventRepository, Source paymentStreamSource, RestTemplate restTemplate) {
        this.eventRepository = eventRepository;
        this.paymentStreamSource = paymentStreamSource;
        this.restTemplate = restTemplate;
    }

    /**
     * Create a new {@link PaymentEvent} and append it to the event log of the referenced {@link Payment}.
     * After the {@link PaymentEvent} has been persisted, send the event to the payment stream. Events can
     * be raised as a blocking or non-blocking operation depending on the {@link ConsistencyModel}.
     *
     * @param paymentId        is the unique identifier for the {@link Payment}
     * @param event            is the {@link PaymentEvent} to create
     * @param consistencyModel is the desired consistency model for the response
     * @return an {@link PaymentEvent} that has been appended to the {@link Payment}'s event log
     */
    public PaymentEvent createEvent(Long paymentId, PaymentEvent event, ConsistencyModel consistencyModel) {
        event = createEvent(paymentId, event);
        return raiseEvent(event, consistencyModel);
    }

    /**
     * Raise an {@link PaymentEvent} that attempts to transition the state of an {@link Payment}.
     *
     * @param event            is an {@link PaymentEvent} that will be raised
     * @param consistencyModel is the consistency model for this request
     * @return an {@link PaymentEvent} that has been appended to the {@link Payment}'s event log
     */
    public PaymentEvent raiseEvent(PaymentEvent event, ConsistencyModel consistencyModel) {
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
     * Raise an asynchronous {@link PaymentEvent} by sending an AMQP message to the payment stream. Any
     * state changes will be applied to the {@link Payment} outside of the current HTTP request context.
     * <p>
     * Use this operation when a workflow can be processed asynchronously outside of the current HTTP
     * request context.
     *
     * @param event is an {@link PaymentEvent} that will be raised
     */
    private void asyncRaiseEvent(PaymentEvent event) {
        // Append the payment event to the stream
        paymentStreamSource.output()
                .send(MessageBuilder
                        .withPayload(getPaymentEventResource(event))
                        .build());
    }

    /**
     * Raise a synchronous {@link PaymentEvent} by sending a HTTP request to the payment stream. The response
     * is a blocking operation, which ensures that the result of a multi-step workflow will not return until
     * the transaction reaches a consistent state.
     * <p>
     * Use this operation when the result of a workflow must be returned within the current HTTP request context.
     *
     * @param event is an {@link PaymentEvent} that will be raised
     * @return an {@link PaymentEvent} which contains the consistent state of an {@link Payment}
     */
    private PaymentEvent raiseEvent(PaymentEvent event) {
        try {
            // Create a new request entity
            RequestEntity<Resource<PaymentEvent>> requestEntity = RequestEntity.post(
                    URI.create("http://localhost:8081/v1/events"))
                    .contentType(MediaTypes.HAL_JSON)
                    .body(getPaymentEventResource(event), Resource.class);

            // Update the payment entity's status
            Payment result = restTemplate.exchange(requestEntity, Payment.class)
                    .getBody();

            log.info(result);
            event.setPayment(result);
        } catch (Exception ex) {
            log.error(ex);
        }

        return event;
    }


    /**
     * Create a new {@link PaymentEvent} and publish it to the payment stream.
     *
     * @param event is the {@link PaymentEvent} to publish to the payment stream
     * @return a hypermedia {@link PaymentEvent} resource
     */
    @CacheEvict(cacheNames = "payment-events", key = "#id.toString()")
    public PaymentEvent createEvent(Long id, PaymentEvent event) {
        // Save new event
        event = addEvent(event);
        Assert.notNull(event, "The event could not be appended to the payment");

        return event;
    }

    /**
     * Get an {@link PaymentEvent} with the supplied identifier.
     *
     * @param id is the unique identifier for the {@link PaymentEvent}
     * @return an {@link PaymentEvent}
     */
    public Resource<PaymentEvent> getEvent(Long id) {
        return getPaymentEventResource(eventRepository.findOne(id));
    }

    /**
     * Update an {@link PaymentEvent} with the supplied identifier.
     *
     * @param id    is the unique identifier for the {@link PaymentEvent}
     * @param event is the {@link PaymentEvent} to update
     * @return the updated {@link PaymentEvent}
     */
    @CacheEvict(cacheNames = "payment-events", key = "#event.getPayment().getPaymentId().toString()")
    public PaymentEvent updateEvent(Long id, PaymentEvent event) {
        Assert.notNull(id);
        Assert.isTrue(event.getId() == null || Objects.equals(id, event.getId()));

        return eventRepository.save(event);
    }

    /**
     * Get {@link PaymentEvents} for the supplied {@link Payment} identifier.
     *
     * @param id is the unique identifier of the {@link Payment}
     * @return a list of {@link PaymentEvent} wrapped in a hypermedia {@link PaymentEvents} resource
     */
    @Cacheable(cacheNames = "payment-events", key = "#id.toString()")
    public List<PaymentEvent> getPaymentEvents(Long id) {
        return eventRepository.findPaymentEventsByPaymentId(id,
                new PageRequest(0, Integer.MAX_VALUE)).getContent();
    }

    /**
     * Gets a hypermedia resource for a {@link PaymentEvent} entity.
     *
     * @param event is the {@link PaymentEvent} to enrich with hypermedia
     * @return a hypermedia resource for the supplied {@link PaymentEvent} entity
     */
    private Resource<PaymentEvent> getPaymentEventResource(PaymentEvent event) {
        return new Resource<PaymentEvent>(event, Arrays.asList(
                linkTo(PaymentController.class)
                        .slash("events")
                        .slash(event.getEventId())
                        .withSelfRel(),
                linkTo(PaymentController.class)
                        .slash("payments")
                        .slash(event.getPayment().getPaymentId())
                        .withRel("payment")));
    }

    /**
     * Add a {@link PaymentEvent} to an {@link Payment} entity.
     *
     * @param event is the {@link PaymentEvent} to append to an {@link Payment} entity
     * @return the newly appended {@link PaymentEvent} entity
     */
    @CacheEvict(cacheNames = "payment-events", key = "#event.getPayment().getPaymentId().toString()")
    private PaymentEvent addEvent(PaymentEvent event) {
        event = eventRepository.saveAndFlush(event);
        return event;
    }
}
