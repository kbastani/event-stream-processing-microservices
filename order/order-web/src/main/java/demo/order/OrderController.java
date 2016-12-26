package demo.order;

import demo.event.EventController;
import demo.event.EventService;
import demo.event.OrderEvent;
import demo.event.OrderEvents;
import org.springframework.hateoas.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1")
public class OrderController {

    private final OrderService orderService;
    private final EventService eventService;

    public OrderController(OrderService orderService, EventService eventService) {
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

    @GetMapping(path = "/orders/{id}")
    public ResponseEntity getOrder(@PathVariable Long id) {
        return Optional.ofNullable(getOrderResource(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(path = "/orders/{id}")
    public ResponseEntity deleteOrder(@PathVariable Long id) {
        return Optional.ofNullable(orderService.deleteOrder(id))
                .map(e -> new ResponseEntity<>(HttpStatus.NO_CONTENT))
                .orElseThrow(() -> new RuntimeException("Order deletion failed"));
    }

    @GetMapping(path = "/orders/{id}/events")
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

    @GetMapping(path = "/orders/{id}/commands")
    public ResponseEntity getOrderCommands(@PathVariable Long id) {
        return Optional.ofNullable(getCommandsResource(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The order could not be found"));
    }

    @GetMapping(path = "/orders/{id}/commands/connectAccount")
    public ResponseEntity connectAccount(@PathVariable Long id, @RequestParam(value = "accountId") Long accountId) {
        return Optional.ofNullable(getOrderResource(orderService.connectAccount(id, accountId)))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @GetMapping(path = "/orders/{id}/commands/connectPayment")
    public ResponseEntity connectPayment(@PathVariable Long id, @RequestParam(value = "paymentId") Long paymentId) {
        return Optional.ofNullable(getOrderResource(orderService.connectPayment(id, paymentId)))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @GetMapping(path = "/orders/{id}/commands/createPayment")
    public ResponseEntity createPayment(@PathVariable Long id) {
        return Optional.ofNullable(getOrderResource(
                orderService.createPayment(id)))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @GetMapping(path = "/orders/{id}/commands/processPayment")
    public ResponseEntity processPayment(@PathVariable Long id) {
        return Optional.ofNullable(getOrderResource(
                orderService.applyCommand(id, OrderCommand.PROCESS_PAYMENT)))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @GetMapping(path = "/orders/{id}/commands/reserveInventory")
    public ResponseEntity reserveInventory(@PathVariable Long id) {
        return Optional.ofNullable(getOrderResource(
                orderService.applyCommand(id, OrderCommand.RESERVE_INVENTORY)))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    /**
     * Retrieves a hypermedia resource for {@link Order} with the specified identifier.
     *
     * @param id is the unique identifier for looking up the {@link Order} entity
     * @return a hypermedia resource for the fetched {@link Order}
     */
    private Resource<Order> getOrderResource(Long id) {
        Resource<Order> orderResource = null;

        // Get the order for the provided id
        Order order = orderService.getOrder(id);

        // If the order exists, wrap the hypermedia response
        if (order != null)
            orderResource = getOrderResource(order);


        return orderResource;
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
        return getOrderResource(orderService.updateOrder(id, order));
    }

    /**
     * Appends an {@link OrderEvent} domain event to the event log of the {@link Order}
     * aggregate with the specified orderId.
     *
     * @param orderId is the unique identifier for the {@link Order}
     * @param event   is the {@link OrderEvent} that attempts to alter the state of the {@link Order}
     * @return a hypermedia resource for the newly appended {@link OrderEvent}
     */
    private Resource<OrderEvent> appendEventResource(Long orderId, OrderEvent event) {
        Resource<OrderEvent> eventResource = null;

        event = orderService.appendEvent(orderId, event);

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

    /**
     * Get the {@link OrderCommand} hypermedia resource that lists the available commands that can be applied
     * to an {@link Order} entity.
     *
     * @param id is the {@link Order} identifier to provide command links for
     * @return an {@link OrderCommands} with a collection of embedded command links
     */
    private OrderCommands getCommandsResource(Long id) {
        // Get the order resource for the identifier
        Resource<Order> orderResource = getOrderResource(id);

        // Create a new order commands hypermedia resource
        OrderCommands commandResource = new OrderCommands();

        // Add order command hypermedia links
        if (orderResource != null) {
            commandResource.add(
                    new Link(new UriTemplate(
                            getCommandLinkBuilder(id)
                                    .slash("connectAccount")
                                    .toUri()
                                    .toString(),
                            new TemplateVariables(
                                    new TemplateVariable("accountId",
                                            TemplateVariable.VariableType.REQUEST_PARAM))), "connectAccount"),
                    getCommandLinkBuilder(id)
                            .slash("reserveInventory")
                            .withRel("reserveInventory"),
                    getCommandLinkBuilder(id)
                            .slash("createPayment")
                            .withRel("createPayment"),
                    new Link(new UriTemplate(
                            getCommandLinkBuilder(id)
                                    .slash("connectPayment")
                                    .toUri()
                                    .toString(),
                            new TemplateVariables(
                                    new TemplateVariable("paymentId",
                                            TemplateVariable.VariableType.REQUEST_PARAM))), "connectPayment"),
                    getCommandLinkBuilder(id)
                            .slash("processPayment")
                            .withRel("processPayment")
            );
        }

        return commandResource;
    }

    /**
     * Get {@link OrderEvents} for the supplied {@link Order} identifier.
     *
     * @param id is the unique identifier of the {@link Order}
     * @return a list of {@link OrderEvent} wrapped in a hypermedia {@link OrderEvents} resource
     */
    private OrderEvents getOrderEventResources(Long id) {
        return new OrderEvents(id, eventService.getOrderEvents(id));
    }

    /**
     * Generate a {@link LinkBuilder} for generating the {@link OrderCommands}.
     *
     * @param id is the unique identifier for a {@link Order}
     * @return a {@link LinkBuilder} for the {@link OrderCommands}
     */
    private LinkBuilder getCommandLinkBuilder(Long id) {
        return linkTo(OrderController.class)
                .slash("orders")
                .slash(id)
                .slash("commands");
    }

    /**
     * Get a hypermedia enriched {@link Order} entity.
     *
     * @param order is the {@link Order} to enrich with hypermedia links
     * @return is a hypermedia enriched resource for the supplied {@link Order} entity
     */
    private Resource<Order> getOrderResource(Order order) {
        Resource<Order> orderResource;

        // Prepare hypermedia response
        orderResource = new Resource<>(order,
                linkTo(OrderController.class)
                        .slash("orders")
                        .slash(order.getOrderId())
                        .withSelfRel(),
                linkTo(OrderController.class)
                        .slash("orders")
                        .slash(order.getOrderId())
                        .slash("events")
                        .withRel("events"),
                getCommandLinkBuilder(order.getOrderId())
                        .withRel("commands")
        );

        if (order.getAccountId() != null)
            orderResource.add(new Link("http://account-service/v1/accounts/" + order.getAccountId(), "account"));

        if (order.getPaymentId() != null)
            orderResource.add(new Link("http://localhost:8082/v1/payments/" + order.getPaymentId(), "payment"));

        return orderResource;
    }
}
