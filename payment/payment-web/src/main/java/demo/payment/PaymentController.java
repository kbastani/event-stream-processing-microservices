package demo.payment;

import demo.event.*;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

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

    public PaymentController(PaymentService paymentService, EventService<PaymentEvent, Long> eventService) {
        this.paymentService = paymentService;
        this.eventService = eventService;
    }

    @PostMapping(path = "/payments")
    public ResponseEntity createPayment(@RequestBody Payment payment) {
        return Optional.ofNullable(createPaymentResource(payment))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Payment creation failed"));
    }

    @PutMapping(path = "/payments/{id}")
    public ResponseEntity updatePayment(@RequestBody Payment payment, @PathVariable Long id) {
        return Optional.ofNullable(updatePaymentResource(id, payment))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Payment update failed"));
    }

    @GetMapping(path = "/payments/{id}")
    public ResponseEntity getPayment(@PathVariable Long id) {
        return Optional.ofNullable(getPaymentResource(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(path = "/payments/{id}")
    public ResponseEntity deletePayment(@PathVariable Long id) {
        return Optional.ofNullable(paymentService.deletePayment(id))
                .map(e -> new ResponseEntity<>(HttpStatus.NO_CONTENT))
                .orElseThrow(() -> new RuntimeException("Payment deletion failed"));
    }

    @GetMapping(path = "/payments/{id}/events")
    public ResponseEntity getPaymentEvents(@PathVariable Long id) {
        return Optional.of(getPaymentEventResources(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Could not get payment events"));
    }

    @PostMapping(path = "/payments/{id}/events")
    public ResponseEntity createPayment(@PathVariable Long id, @RequestBody PaymentEvent event) {
        return Optional.ofNullable(appendEventResource(id, event))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Append payment event failed"));
    }

    @GetMapping(path = "/payments/{id}/commands")
    public ResponseEntity getPaymentCommands(@PathVariable Long id) {
        return Optional.ofNullable(getCommandsResource(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The payment could not be found"));
    }

    @GetMapping(path = "/payments/{id}/commands/connectOrder")
    public ResponseEntity connectOrder(@PathVariable Long id) {
        return Optional.ofNullable(getPaymentResource(
                paymentService.applyCommand(id, PaymentCommand.CONNECT_ORDER)))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @GetMapping(path = "/payments/{id}/commands/processPayment")
    public ResponseEntity processPayment(@PathVariable Long id) {
        return Optional.ofNullable(getPaymentResource(
                paymentService.applyCommand(id, PaymentCommand.PROCESS_PAYMENT)))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    /**
     * Retrieves a hypermedia resource for {@link Payment} with the specified identifier.
     *
     * @param id is the unique identifier for looking up the {@link Payment} entity
     * @return a hypermedia resource for the fetched {@link Payment}
     */
    private Resource<Payment> getPaymentResource(Long id) {
        Resource<Payment> paymentResource = null;

        // Get the payment for the provided id
        Payment payment = paymentService.getPayment(id);

        // If the payment exists, wrap the hypermedia response
        if (payment != null)
            paymentResource = getPaymentResource(payment);

        return paymentResource;
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

        return getPaymentResource(payment);
    }

    /**
     * Update a {@link Payment} entity for the provided identifier.
     *
     * @param id      is the unique identifier for the {@link Payment} update
     * @param payment is the entity representation containing any updated {@link Payment} fields
     * @return a hypermedia resource for the updated {@link Payment}
     */
    private Resource<Payment> updatePaymentResource(Long id, Payment payment) {
        return getPaymentResource(paymentService.updatePayment(id, payment));
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

        event = paymentService.appendEvent(paymentId, event);

        if (event != null) {
            eventResource = new Resource<>(event,
                    linkTo(EventController.class)
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

    /**
     * Get the {@link PaymentCommand} hypermedia resource that lists the available commands that can be applied to a
     * {@link Payment} entity.
     *
     * @param id is the {@link Payment} identifier to provide command links for
     * @return an {@link PaymentCommands} with a collection of embedded command links
     */
    private PaymentCommands getCommandsResource(Long id) {
        // Get the payment resource for the identifier
        Resource<Payment> paymentResource = getPaymentResource(id);

        // Create a new payment commands hypermedia resource
        PaymentCommands commandResource = new PaymentCommands();

        // Add payment command hypermedia links
        if (paymentResource != null) {
            commandResource.add(
                    getCommandLinkBuilder(id)
                            .slash("connectOrder")
                            .withRel("connectOrder"),
                    getCommandLinkBuilder(id)
                            .slash("processPayment")
                            .withRel("processPayment")
            );
        }

        return commandResource;
    }

    private Events getPaymentEventResources(Long id) {
        return eventService.find(id);
    }

    /**
     * Generate a {@link LinkBuilder} for generating the {@link PaymentCommands}.
     *
     * @param id is the unique identifier for a {@link Payment}
     * @return a {@link LinkBuilder} for the {@link PaymentCommands}
     */
    private LinkBuilder getCommandLinkBuilder(Long id) {
        return linkTo(PaymentController.class)
                .slash("payments")
                .slash(id)
                .slash("commands");
    }

    /**
     * Get a hypermedia enriched {@link Payment} entity.
     *
     * @param payment is the {@link Payment} to enrich with hypermedia links
     * @return is a hypermedia enriched resource for the supplied {@link Payment} entity
     */
    private Resource<Payment> getPaymentResource(Payment payment) {
        Resource<Payment> paymentResource;

        // Prepare hypermedia response
        paymentResource = new Resource<>(payment,
                linkTo(PaymentController.class)
                        .slash("payments")
                        .slash(payment.getPaymentId())
                        .withSelfRel(),
                linkTo(PaymentController.class)
                        .slash("payments")
                        .slash(payment.getPaymentId())
                        .slash("events")
                        .withRel("events"),
                getCommandLinkBuilder(payment.getPaymentId())
                        .withRel("commands")
        );

        return paymentResource;
    }
}
