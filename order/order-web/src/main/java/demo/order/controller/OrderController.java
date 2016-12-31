package demo.order.controller;

import demo.event.EventController;
import demo.event.EventService;
import demo.event.Events;
import demo.event.OrderEvent;
import demo.order.Order;
import demo.order.OrderService;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1")
public class OrderController {

    private final OrderService orderService;
    private final EventService<OrderEvent, Long> eventService;

    public OrderController(OrderService orderService, EventService<OrderEvent, Long> eventService) {
        this.orderService = orderService;
        this.eventService = eventService;
    }

    @PostMapping(path = "/orders")
    public ResponseEntity createOrder(@RequestBody Order order) {
        return Optional.ofNullable(createOrderResource(order))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Order creation failed"));
    }

    @PutMapping(path = "/orders/{id}")
    public ResponseEntity updateOrder(@RequestBody Order order, @PathVariable Long id) {
        return Optional.ofNullable(updateOrderResource(id, order))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Order update failed"));
    }

    @RequestMapping(path = "/orders/{id}")
    public ResponseEntity getOrder(@PathVariable Long id) {
        return Optional.ofNullable(getOrderResource(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(path = "/orders/{id}")
    public ResponseEntity deleteOrder(@PathVariable Long id) {
        return Optional.ofNullable(orderService.delete(id))
                .map(e -> new ResponseEntity<>(HttpStatus.NO_CONTENT))
                .orElseThrow(() -> new RuntimeException("Order deletion failed"));
    }

    @RequestMapping(path = "/orders/{id}/events")
    public ResponseEntity getOrderEvents(@PathVariable Long id) {
        return Optional.of(getOrderEventResources(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Could not get order events"));
    }

    @PostMapping(path = "/orders/{id}/events")
    public ResponseEntity createOrder(@PathVariable Long id, @RequestBody OrderEvent event) {
        return Optional.ofNullable(appendEventResource(id, event))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Append order event failed"));
    }

    @RequestMapping(path = "/orders/{id}/commands")
    public ResponseEntity getCommands(@PathVariable Long id) {
        return Optional.ofNullable(getCommandsResources(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The order could not be found"));
    }

    @RequestMapping(path = "/orders/{id}/commands/connectAccount")
    public ResponseEntity connectAccount(@PathVariable Long id, @RequestParam(value = "accountId") Long accountId) {
        return Optional.ofNullable(orderService.get(id)
                .connectAccount(accountId))
                .map(e -> new ResponseEntity<>(getOrderResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @RequestMapping(path = "/orders/{id}/commands/connectPayment")
    public ResponseEntity connectPayment(@PathVariable Long id, @RequestParam(value = "paymentId") Long paymentId) {
        return Optional.ofNullable(orderService.get(id)
                .connectPayment(paymentId))
                .map(e -> new ResponseEntity<>(getOrderResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @RequestMapping(path = "/orders/{id}/commands/createPayment")
    public ResponseEntity createPayment(@PathVariable Long id) {
        return Optional.ofNullable(orderService.get(id)
                .createPayment())
                .map(e -> new ResponseEntity<>(getOrderResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @RequestMapping(path = "/orders/{id}/commands/processPayment")
    public ResponseEntity processPayment(@PathVariable Long id) {
        return Optional.ofNullable(orderService.get(id)
                .processPayment())
                .map(e -> new ResponseEntity<>(getOrderResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @RequestMapping(path = "/orders/{id}/commands/reserveInventory")
    public ResponseEntity reserveInventory(@PathVariable Long id) {
        return Optional.ofNullable(orderService.get(id)
                .reserveInventory(id))
                .map(e -> new ResponseEntity<>(getOrderResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    /**
     * Retrieves a hypermedia resource for {@link Order} with the specified identifier.
     *
     * @param id is the unique identifier for looking up the {@link Order} entity
     * @return a hypermedia resource for the fetched {@link Order}
     */
    private Resource<Order> getOrderResource(Long id) {
        // Get the order for the provided id
        Order order = orderService.get(id);

        return getOrderResource(order);
    }

    /**
     * Creates a new {@link Order} entity and persists the result to the repository.
     *
     * @param order is the {@link Order} model used to create a new order
     * @return a hypermedia resource for the newly created {@link Order}
     */
    private Resource<Order> createOrderResource(Order order) {
        Assert.notNull(order, "Order body must not be null");

        // Create the new order
        order = orderService.registerOrder(order);

        return getOrderResource(order);
    }

    /**
     * Update a {@link Order} entity for the provided identifier.
     *
     * @param id    is the unique identifier for the {@link Order} update
     * @param order is the entity representation containing any updated {@link Order} fields
     * @return a hypermedia resource for the updated {@link Order}
     */
    private Resource<Order> updateOrderResource(Long id, Order order) {
        order.setIdentity(id);
        return getOrderResource(orderService.update(order));
    }

    /**
     * Appends an {@link OrderEvent} domain event to the event log of the {@link Order} aggregate with the
     * specified orderId.
     *
     * @param orderId is the unique identifier for the {@link Order}
     * @param event   is the {@link OrderEvent} that attempts to alter the state of the {@link Order}
     * @return a hypermedia resource for the newly appended {@link OrderEvent}
     */
    private Resource<OrderEvent> appendEventResource(Long orderId, OrderEvent event) {
        Resource<OrderEvent> eventResource = null;

        orderService.get(orderId)
                .sendAsyncEvent(event);

        if (event != null) {
            eventResource = new Resource<>(event,
                    linkTo(EventController.class)
                            .slash("events")
                            .slash(event.getEventId())
                            .withSelfRel(),
                    linkTo(OrderController.class)
                            .slash("orders")
                            .slash(orderId)
                            .withRel("order")
            );
        }

        return eventResource;
    }

    private Events getOrderEventResources(Long id) {
        return eventService.find(id);
    }

    private LinkBuilder linkBuilder(String name, Long id) {
        Method method;

        try {
            method = OrderController.class.getMethod(name, Long.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return linkTo(OrderController.class, method, id);
    }

    /**
     * Get a hypermedia enriched {@link Order} entity.
     *
     * @param order is the {@link Order} to enrich with hypermedia links
     * @return is a hypermedia enriched resource for the supplied {@link Order} entity
     */
    private Resource<Order> getOrderResource(Order order) {
        Assert.notNull(order, "Order must not be null");

        // Add command link
        order.add(linkBuilder("getCommands", order.getIdentity()).withRel("commands"));

        // Add get events link
        order.add(linkBuilder("getOrderEvents", order.getIdentity()).withRel("events"));

        if (order.getAccountId() != null)
            order.add(new Link("http://account-service/v1/accounts/" + order.getAccountId(), "account"));

        if (order.getPaymentId() != null)
            order.add(new Link("http://localhost:8082/v1/payments/" + order.getPaymentId(), "payment"));

        return new Resource<>(order);
    }

    private ResourceSupport getCommandsResources(Long id) {
        Order order = new Order();
        order.setIdentity(id);
        return new Resource<>(order.getCommands());
    }
}
