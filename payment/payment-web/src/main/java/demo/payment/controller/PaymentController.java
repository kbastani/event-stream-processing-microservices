package demo.payment.controller;

import demo.event.EventService;
import demo.event.Events;
import demo.payment.domain.Payment;
import demo.payment.domain.PaymentService;
import demo.payment.event.PaymentEvent;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.hateoas.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * The REST API for managing {@link Payment} entities and {@link PaymentEvent}s.
 *
 * @author Kenny Bastani
 */
@RestController
@RequestMapping("/v1")
@ExposesResourceFor(Payment.class)
public class PaymentController {

    private final PaymentService paymentService;
    private final EventService<PaymentEvent, Long> eventService;
    private final DiscoveryClient discoveryClient;

    public PaymentController(PaymentService paymentService, EventService<PaymentEvent, Long> eventService,
            DiscoveryClient discoveryClient) {
        this.paymentService = paymentService;
        this.eventService = eventService;
        this.discoveryClient = discoveryClient;
    }

    @RequestMapping(path = "/payments", method = RequestMethod.POST)
    public ResponseEntity createPayment(@RequestBody Payment payment) {
        return Optional.ofNullable(createPaymentResource(payment))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Payment creation failed"));
    }

    @RequestMapping(path = "/payments/{id}", method = RequestMethod.PUT)
    public ResponseEntity updatePayment(@RequestBody Payment payment, @PathVariable Long id) {
        return Optional.ofNullable(updatePaymentResource(id, payment))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Payment update failed"));
    }

    @RequestMapping(path = "/payments/{id}", method = RequestMethod.GET)
    public ResponseEntity getPayment(@PathVariable Long id) {
        return Optional.ofNullable(getPaymentResource(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(path = "/payments/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deletePayment(@PathVariable Long id) {
        return Optional.ofNullable(paymentService.delete(id))
                .map(e -> new ResponseEntity<>(HttpStatus.NO_CONTENT))
                .orElseThrow(() -> new RuntimeException("Payment deletion failed"));
    }

    @RequestMapping(path = "/payments/{id}/events", method = RequestMethod.GET)
    public ResponseEntity getPaymentEvents(@PathVariable Long id) {
        return Optional.ofNullable(getPaymentEventResources(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Could not get payment events"));
    }

    @RequestMapping(path = "/payments/{id}/events/{eventId}")
    public ResponseEntity getPaymentEvent(@PathVariable Long id, @PathVariable Long eventId) {
        return Optional.of(getEventResource(eventId))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Could not get payment events"));
    }

    @RequestMapping(path = "/payments/{id}/events", method = RequestMethod.POST)
    public ResponseEntity createPaymentEvents(@PathVariable Long id, @RequestBody PaymentEvent event) {
        return Optional.ofNullable(appendEventResource(id, event))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Append payment event failed"));
    }

    @RequestMapping(path = "/payments/{id}/commands")
    public ResponseEntity getCommands(@PathVariable Long id) {
        return Optional.ofNullable(getCommandsResources(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The payment could not be found"));
    }

    @RequestMapping(path = "/payments/{id}/commands/connectOrder")
    public ResponseEntity connectOrder(@PathVariable Long id, @RequestParam(value = "orderId") Long orderId) {
        return Optional.of(paymentService.get(id)
                .connectOrder(orderId))
                .map(e -> new ResponseEntity<>(getPaymentResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @RequestMapping(path = "/payments/{id}/commands/processPayment")
    public ResponseEntity processPayment(@PathVariable Long id) {
        return Optional.of(paymentService.get(id)
                .processPayment())
                .map(e -> new ResponseEntity<>(getPaymentResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    /**
     * Retrieves a hypermedia resource for {@link Payment} with the specified identifier.
     *
     * @param id is the unique identifier for looking up the {@link Payment} entity
     * @return a hypermedia resource for the fetched {@link Payment}
     */
    private Resource<Payment> getPaymentResource(Long id) {
        // Get the payment for the provided id
        Payment payment = paymentService.get(id);

        return getPaymentResource(payment);
    }

    /**
     * Creates a new {@link Payment} entity and persists the result to the repository.
     *
     * @param payment is the {@link Payment} model used to create a new payment
     * @return a hypermedia resource for the newly created {@link Payment}
     */
    private Resource<Payment> createPaymentResource(Payment payment) {
        Assert.notNull(payment, "Payment body must not be null");

        // Create the new payment
        payment = paymentService.registerPayment(payment);

        return new Resource<>(payment);
    }

    /**
     * Update a {@link Payment} entity for the provided identifier.
     *
     * @param id      is the unique identifier for the {@link Payment} update
     * @param payment is the entity representation containing any updated {@link Payment} fields
     * @return a hypermedia resource for the updated {@link Payment}
     */
    private Resource<Payment> updatePaymentResource(Long id, Payment payment) {
        payment.setIdentity(id);
        return getPaymentResource(paymentService.update(payment));
    }

    /**
     * Appends an {@link PaymentEvent} domain event to the event log of the {@link Payment} aggregate with the
     * specified paymentId.
     *
     * @param paymentId is the unique identifier for the {@link Payment}
     * @param event     is the {@link PaymentEvent} that attempts to alter the state of the {@link Payment}
     * @return a hypermedia resource for the newly appended {@link PaymentEvent}
     */
    private Resource<PaymentEvent> appendEventResource(Long paymentId, PaymentEvent event) {
        Resource<PaymentEvent> eventResource = null;

        event = paymentService.get(paymentId).sendEvent(event);

        if (event != null) {
            eventResource = new Resource<>(event,
                    linkTo(PaymentController.class)
                            .slash("payments")
                            .slash(paymentId)
                            .slash("events")
                            .slash(event.getEventId())
                            .withSelfRel(),
                    linkTo(PaymentController.class)
                            .slash("payments")
                            .slash(paymentId)
                            .withRel("payment")
            );
        }

        return eventResource;
    }

    private PaymentEvent getEventResource(Long eventId) {
        return eventService.findOne(eventId);
    }

    private Events getPaymentEventResources(Long id) {
        return eventService.find(id);
    }

    private LinkBuilder linkBuilder(String name, Long id) {
        Method method;

        try {
            method = PaymentController.class.getMethod(name, Long.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return linkTo(PaymentController.class, method, id);
    }

    /**
     * Get a hypermedia enriched {@link Payment} entity.
     *
     * @param payment is the {@link Payment} to enrich with hypermedia links
     * @return is a hypermedia enriched resource for the supplied {@link Payment} entity
     */
    private Resource<Payment> getPaymentResource(Payment payment) {
        Assert.notNull(payment, "Payment must not be null");

        if (!payment.hasLink("commands")) {
            // Add command link
            payment.add(linkBuilder("getCommands", payment.getIdentity()).withRel("commands"));
        }

        if (!payment.hasLink("events")) {
            // Add get events link
            payment.add(linkBuilder("getPaymentEvents", payment.getIdentity()).withRel("events"));
        }

        // Add remote payment link
        if (payment.getOrderId() != null && !payment.hasLink("order")) {
            Link result = getRemoteLink("order-web", "/v1/orders/{id}", payment.getOrderId(), "order");
            if (result != null)
                payment.add(result);
        }

        return new Resource<>(payment);
    }

    private ResourceSupport getCommandsResources(Long id) {
        Payment payment = new Payment();
        payment.setIdentity(id);
        return new Resource<>(payment.getCommands());
    }

    private Link getRemoteLink(String service, String relative, Object identifier, String rel) {
        Link result = null;
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(service);
        if (serviceInstances.size() > 0) {
            ServiceInstance serviceInstance = serviceInstances.get(new Random().nextInt(serviceInstances.size()));
            result = new Link(new UriTemplate(serviceInstance.getUri()
                    .toString()
                    .concat(relative)).with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                    .expand(identifier)
                    .toString())
                    .withRel(rel);
        }
        return result;
    }
}
