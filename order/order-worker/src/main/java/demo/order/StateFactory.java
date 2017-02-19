package demo.order;

import demo.order.domain.Order;
import demo.order.domain.OrderStatus;
import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import demo.order.event.OrderEvents;
import org.apache.log4j.Logger;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

@Service
public class StateFactory {

    final private Logger log = Logger.getLogger(StateFactory.class);
    final private StateService stateService;
    final private RestTemplate restTemplate;
    final private DiscoveryClient discoveryClient;

    public StateFactory(StateService stateService, RestTemplate restTemplate, DiscoveryClient discoveryClient) {
        this.stateService = stateService;
        this.restTemplate = restTemplate;
        this.discoveryClient = discoveryClient;
    }

    public Order apply(OrderEvent orderEvent) {
        Assert.notNull(orderEvent, "Cannot apply a null event");
        Assert.notNull(orderEvent.getId(), "The event payload's identity link was not found");

        List<ServiceInstance> serviceInstances = discoveryClient.getInstances("order-web");
        Assert.notEmpty(serviceInstances, "No instances available for order-web");
        Integer instanceNum = new Random().nextInt(serviceInstances.size());
        ServiceInstance orderService = serviceInstances.get(instanceNum);

        URI orderHref = getLoadBalanceUri(orderService, URI.create(orderEvent.getLink("order").getHref()));
        URI selfHref = getLoadBalanceUri(orderService, URI.create(orderEvent.getLink("self").getHref()));

        orderEvent.getLinks()
                .replaceAll(a -> Objects.equals(a.getRel(), "order") ? new Link(orderHref
                        .toString(), "order") : a);

        orderEvent.getLinks()
                .replaceAll(a -> Objects.equals(a.getRel(), "self") ? new Link(selfHref
                        .toString(), "self") : a);

        StateMachine<OrderStatus, OrderEventType> stateMachine = getStateMachine(orderEvent);
        stateMachine.stop();

        return stateMachine.getExtendedState().get("order", Order.class);
    }

    private URI getLoadBalanceUri(ServiceInstance serviceInstance, URI uri) {
        return URI.create(uri.toString()
                .replace(uri.getHost(), serviceInstance.getHost())
                .replace(":" + uri.getPort(), ":" + String.valueOf(serviceInstance.getPort())));
    }

    private StateMachine<OrderStatus, OrderEventType> getStateMachine(OrderEvent orderEvent) {
        Link eventId = orderEvent.getId();
        log.info(String.format("Order event received: %s", eventId));

        StateMachine<OrderStatus, OrderEventType> stateMachine;
        Map<String, Object> contextMap;
        OrderEvents eventLog;

        eventLog = getEventLog(orderEvent);
        contextMap = getEventHeaders(orderEvent);
        stateMachine = stateService.newStateMachine();

        // Replicate the aggregate state
        eventLog.getContent().stream()
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .forEach(e -> stateMachine.sendEvent(MessageBuilder.createMessage(e.getType(), e.getId()
                        .equals(eventId) ? new MessageHeaders(contextMap) : new MessageHeaders(null))));

        return stateMachine;
    }

    private Map<String, Object> getEventHeaders(OrderEvent orderEvent) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("event", orderEvent);
        return headerMap;
    }

    private OrderEvents getEventLog(OrderEvent event) {

        // Replace the host and port of the link with an available instance
        URI orderHref = URI.create(event.getLink("order")
                .getHref());

        // Follow the hypermedia link to fetch the attached order
        Traverson traverson = new Traverson(orderHref,
                MediaTypes.HAL_JSON
        );

        traverson.setRestOperations(restTemplate);

        // Get the event log for the attached order resource
        return traverson.follow("events")
                .toEntity(OrderEvents.class)
                .getBody();
    }
}
