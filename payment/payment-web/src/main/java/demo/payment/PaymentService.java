package demo.payment;

import demo.domain.Service;
import demo.event.EventService;
import demo.event.PaymentEvent;
import demo.event.PaymentEventType;
import demo.util.ConsistencyModel;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * The {@link PaymentService} provides transactional support for managing {@link Payment} entities. This service also
 * provides event sourcing support for {@link PaymentEvent}. Events can be appended to an {@link Payment}, which
 * contains a append-only log of actions that can be used to support remediation for distributed transactions that
 * encountered a partial failure.
 *
 * @author Kenny Bastani
 */
@org.springframework.stereotype.Service
public class PaymentService extends Service<Payment> {

    private final PaymentRepository paymentRepository;
    private final EventService<PaymentEvent, Long> eventService;

    public PaymentService(PaymentRepository paymentRepository, EventService<PaymentEvent, Long> eventService) {
        this.paymentRepository = paymentRepository;
        this.eventService = eventService;
    }

    public Payment registerPayment(Payment payment) {

        payment = createPayment(payment);

        // Trigger the payment creation event
        PaymentEvent event = appendEvent(payment.getIdentity(),
                new PaymentEvent(PaymentEventType.PAYMENT_CREATED));

        // Attach payment identifier
        event.getEntity().setIdentity(payment.getIdentity());

        // Return the result
        return event.getEntity();
    }

    /**
     * Create a new {@link Payment} entity.
     *
     * @param payment is the {@link Payment} to create
     * @return the newly created {@link Payment}
     */
    public Payment createPayment(Payment payment) {

        // Save the payment to the repository
        payment = paymentRepository.saveAndFlush(payment);

        return payment;
    }

    /**
     * Get an {@link Payment} entity for the supplied identifier.
     *
     * @param id is the unique identifier of a {@link Payment} entity
     * @return an {@link Payment} entity
     */
    public Payment getPayment(Long id) {
        return paymentRepository.findOne(id);
    }

    /**
     * Update an {@link Payment} entity with the supplied identifier.
     *
     * @param id      is the unique identifier of the {@link Payment} entity
     * @param payment is the {@link Payment} containing updated fields
     * @return the updated {@link Payment} entity
     */
    public Payment updatePayment(Long id, Payment payment) {
        Assert.notNull(id, "Payment id must be present in the resource URL");
        Assert.notNull(payment, "Payment request body cannot be null");

        if (payment.getIdentity() != null) {
            Assert.isTrue(Objects.equals(id, payment.getIdentity()),
                    "The payment id in the request body must match the resource URL");
        } else {
            payment.setIdentity(id);
        }

        Assert.state(paymentRepository.exists(id),
                "The payment with the supplied id does not exist");

        Payment currentPayment = paymentRepository.findOne(id);
        currentPayment.setStatus(payment.getStatus());

        return paymentRepository.save(currentPayment);
    }

    /**
     * Delete the {@link Payment} with the supplied identifier.
     *
     * @param id is the unique identifier for the {@link Payment}
     */
    public Boolean deletePayment(Long id) {
        Assert.state(paymentRepository.exists(id),
                "The payment with the supplied id does not exist");
        this.paymentRepository.delete(id);
        return true;
    }

    /**
     * Append a new {@link PaymentEvent} to the {@link Payment} reference for the supplied identifier.
     *
     * @param paymentId is the unique identifier for the {@link Payment}
     * @param event     is the {@link PaymentEvent} to append to the {@link Payment} entity
     * @return the newly appended {@link PaymentEvent}
     */
    public PaymentEvent appendEvent(Long paymentId, PaymentEvent event) {
        return appendEvent(paymentId, event, ConsistencyModel.BASE);
    }

    /**
     * Append a new {@link PaymentEvent} to the {@link Payment} reference for the supplied identifier.
     *
     * @param paymentId is the unique identifier for the {@link Payment}
     * @param event     is the {@link PaymentEvent} to append to the {@link Payment} entity
     * @return the newly appended {@link PaymentEvent}
     */
    public PaymentEvent appendEvent(Long paymentId, PaymentEvent event, ConsistencyModel consistencyModel) {

        // Get the entity
        Payment payment = getPayment(paymentId);
        Assert.notNull(payment, "The payment with the supplied id does not exist");

        // Add the entity to the event
        event.setEntity(payment);
        event = eventService.save(paymentId, event);

        // Add the event to the entity
        payment.getEvents().add(event);
        paymentRepository.saveAndFlush(payment);

        // Applies the event for the chosen consistency model
        switch (consistencyModel) {
            case BASE:
                eventService.sendAsync(event);
                break;
            case ACID:
                event = eventService.send(event);
                break;
        }

        return event;
    }
}
