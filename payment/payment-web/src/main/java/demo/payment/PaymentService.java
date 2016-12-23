package demo.payment;

import demo.event.ConsistencyModel;
import demo.event.EventService;
import demo.event.PaymentEvent;
import demo.event.PaymentEventType;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * The {@link PaymentService} provides transactional support for managing {@link Payment}
 * entities. This service also provides event sourcing support for {@link PaymentEvent}.
 * Events can be appended to an {@link Payment}, which contains a append-only log of
 * actions that can be used to support remediation for distributed transactions that encountered
 * a partial failure.
 *
 * @author kbastani
 */
@Service
@CacheConfig(cacheNames = {"payments"})
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final EventService eventService;
    private final CacheManager cacheManager;

    public PaymentService(PaymentRepository paymentRepository, EventService eventService, CacheManager cacheManager) {
        this.paymentRepository = paymentRepository;
        this.eventService = eventService;
        this.cacheManager = cacheManager;
    }

    @CacheEvict(cacheNames = "payments", key = "#payment.getPaymentId().toString()")
    public Payment registerPayment(Payment payment) {

        payment = createPayment(payment);

        cacheManager.getCache("payments")
                .evict(payment.getPaymentId());

        // Trigger the payment creation event
        PaymentEvent event = appendEvent(payment.getPaymentId(),
                new PaymentEvent(PaymentEventType.PAYMENT_CREATED));

        // Attach payment identifier
        event.getPayment().setPaymentId(payment.getPaymentId());

        // Return the result
        return event.getPayment();
    }

    /**
     * Create a new {@link Payment} entity.
     *
     * @param payment is the {@link Payment} to create
     * @return the newly created {@link Payment}
     */
    @CacheEvict(cacheNames = "payments", key = "#payment.getPaymentId().toString()")
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
    @Cacheable(cacheNames = "payments", key = "#id.toString()")
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
    @CachePut(cacheNames = "payments", key = "#id.toString()")
    public Payment updatePayment(Long id, Payment payment) {
        Assert.notNull(id, "Payment id must be present in the resource URL");
        Assert.notNull(payment, "Payment request body cannot be null");

        if (payment.getPaymentId() != null) {
            Assert.isTrue(Objects.equals(id, payment.getPaymentId()),
                    "The payment id in the request body must match the resource URL");
        } else {
            payment.setPaymentId(id);
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
    @CacheEvict(cacheNames = "payments", key = "#id.toString()")
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
        return appendEvent(paymentId, event, ConsistencyModel.ACID);
    }

    /**
     * Append a new {@link PaymentEvent} to the {@link Payment} reference for the supplied identifier.
     *
     * @param paymentId is the unique identifier for the {@link Payment}
     * @param event     is the {@link PaymentEvent} to append to the {@link Payment} entity
     * @return the newly appended {@link PaymentEvent}
     */
    public PaymentEvent appendEvent(Long paymentId, PaymentEvent event, ConsistencyModel consistencyModel) {
        Payment payment = getPayment(paymentId);
        Assert.notNull(payment, "The payment with the supplied id does not exist");
        event.setPayment(payment);
        event = eventService.createEvent(paymentId, event);
        payment.getEvents().add(event);
        paymentRepository.saveAndFlush(payment);
        eventService.raiseEvent(event, consistencyModel);
        return event;
    }

    /**
     * Apply an {@link PaymentCommand} to the {@link Payment} with a specified identifier.
     *
     * @param id             is the unique identifier of the {@link Payment}
     * @param paymentCommand is the command to apply to the {@link Payment}
     * @return a hypermedia resource containing the updated {@link Payment}
     */
    @CachePut(cacheNames = "payments", key = "#id.toString()")
    public Payment applyCommand(Long id, PaymentCommand paymentCommand) {
        Payment payment = getPayment(id);

        Assert.notNull(payment, "The payment for the supplied id could not be found");

        PaymentStatus status = payment.getStatus();

        // TODO: Implement

        return payment;
    }
}
