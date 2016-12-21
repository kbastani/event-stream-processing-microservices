package demo.event;

import demo.order.Order;
import demo.order.OrderController;
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
 * The {@link EventService} provides transactional service methods for {@link OrderEvent}
 * entities of the Order Service. Order domain events are generated with a {@link OrderEventType},
 * and action logs are appended to the {@link OrderEvent}.
 *
 * @author kbastani
 */
@Service
@CacheConfig(cacheNames = {"order-events"})
public class EventService {

    private final Logger log = Logger.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final Source orderStreamSource;
    private final RestTemplate restTemplate;

    public EventService(EventRepository eventRepository, Source orderStreamSource, RestTemplate restTemplate) {
        this.eventRepository = eventRepository;
        this.orderStreamSource = orderStreamSource;
        this.restTemplate = restTemplate;
    }

    /**
     * Create a new {@link OrderEvent} and append it to the event log of the referenced {@link Order}.
     * After the {@link OrderEvent} has been persisted, send the event to the order stream. Events can
     * be raised as a blocking or non-blocking operation depending on the {@link ConsistencyModel}.
     *
     * @param orderId          is the unique identifier for the {@link Order}
     * @param event            is the {@link OrderEvent} to create
     * @param consistencyModel is the desired consistency model for the response
     * @return an {@link OrderEvent} that has been appended to the {@link Order}'s event log
     */
    public OrderEvent createEvent(Long orderId, OrderEvent event, ConsistencyModel consistencyModel) {
        event = createEvent(orderId, event);
        return raiseEvent(event, consistencyModel);
    }

    /**
     * Raise an {@link OrderEvent} that attempts to transition the state of an {@link Order}.
     *
     * @param event            is an {@link OrderEvent} that will be raised
     * @param consistencyModel is the consistency model for this request
     * @return an {@link OrderEvent} that has been appended to the {@link Order}'s event log
     */
    public OrderEvent raiseEvent(OrderEvent event, ConsistencyModel consistencyModel) {
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
     * Raise an asynchronous {@link OrderEvent} by sending an AMQP message to the order stream. Any
     * state changes will be applied to the {@link Order} outside of the current HTTP request context.
     * <p>
     * Use this operation when a workflow can be processed asynchronously outside of the current HTTP
     * request context.
     *
     * @param event is an {@link OrderEvent} that will be raised
     */
    private void asyncRaiseEvent(OrderEvent event) {
        // Append the order event to the stream
        orderStreamSource.output()
                .send(MessageBuilder
                        .withPayload(getOrderEventResource(event))
                        .build());
    }

    /**
     * Raise a synchronous {@link OrderEvent} by sending a HTTP request to the order stream. The response
     * is a blocking operation, which ensures that the result of a multi-step workflow will not return until
     * the transaction reaches a consistent state.
     * <p>
     * Use this operation when the result of a workflow must be returned within the current HTTP request context.
     *
     * @param event is an {@link OrderEvent} that will be raised
     * @return an {@link OrderEvent} which contains the consistent state of an {@link Order}
     */
    private OrderEvent raiseEvent(OrderEvent event) {
        try {
            // Create a new request entity
            RequestEntity<Resource<OrderEvent>> requestEntity = RequestEntity.post(
                    URI.create("http://localhost:8081/v1/events"))
                    .contentType(MediaTypes.HAL_JSON)
                    .body(getOrderEventResource(event), Resource.class);

            // Update the order entity's status
            Order result = restTemplate.exchange(requestEntity, Order.class)
                    .getBody();

            log.info(result);
            event.setOrder(result);
        } catch (Exception ex) {
            log.error(ex);
        }

        return event;
    }


    /**
     * Create a new {@link OrderEvent} and publish it to the order stream.
     *
     * @param event is the {@link OrderEvent} to publish to the order stream
     * @return a hypermedia {@link OrderEvent} resource
     */
    @CacheEvict(cacheNames = "order-events", key = "#id.toString()")
    public OrderEvent createEvent(Long id, OrderEvent event) {
        // Save new event
        event = addEvent(event);
        Assert.notNull(event, "The event could not be appended to the order");

        return event;
    }

    /**
     * Get an {@link OrderEvent} with the supplied identifier.
     *
     * @param id is the unique identifier for the {@link OrderEvent}
     * @return an {@link OrderEvent}
     */
    public Resource<OrderEvent> getEvent(Long id) {
        return getOrderEventResource(eventRepository.findOne(id));
    }

    /**
     * Update an {@link OrderEvent} with the supplied identifier.
     *
     * @param id    is the unique identifier for the {@link OrderEvent}
     * @param event is the {@link OrderEvent} to update
     * @return the updated {@link OrderEvent}
     */
    @CacheEvict(cacheNames = "order-events", key = "#event.order().getOrderId().toString()")
    public OrderEvent updateEvent(Long id, OrderEvent event) {
        Assert.notNull(id);
        Assert.isTrue(event.getId() == null || Objects.equals(id, event.getId()));

        return eventRepository.save(event);
    }

    /**
     * Get {@link OrderEvents} for the supplied {@link Order} identifier.
     *
     * @param id is the unique identifier of the {@link Order}
     * @return a list of {@link OrderEvent} wrapped in a hypermedia {@link OrderEvents} resource
     */
    @Cacheable(cacheNames = "order-events", key = "#id.toString()")
    public List<OrderEvent> getOrderEvents(Long id) {
        return eventRepository.findOrderEventsByOrderId(id,
                new PageRequest(0, Integer.MAX_VALUE)).getContent();
    }

    /**
     * Gets a hypermedia resource for a {@link OrderEvent} entity.
     *
     * @param event is the {@link OrderEvent} to enrich with hypermedia
     * @return a hypermedia resource for the supplied {@link OrderEvent} entity
     */
    private Resource<OrderEvent> getOrderEventResource(OrderEvent event) {
        return new Resource<OrderEvent>(event, Arrays.asList(
                linkTo(OrderController.class)
                        .slash("events")
                        .slash(event.getEventId())
                        .withSelfRel(),
                linkTo(OrderController.class)
                        .slash("orders")
                        .slash(event.getOrder().getOrderId())
                        .withRel("order")));
    }

    /**
     * Add a {@link OrderEvent} to an {@link Order} entity.
     *
     * @param event is the {@link OrderEvent} to append to an {@link Order} entity
     * @return the newly appended {@link OrderEvent} entity
     */
    @CacheEvict(cacheNames = "order-events", key = "#event.order().getOrderId().toString()")
    private OrderEvent addEvent(OrderEvent event) {
        event = eventRepository.saveAndFlush(event);
        return event;
    }
}
