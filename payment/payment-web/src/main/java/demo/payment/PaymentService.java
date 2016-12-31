package demo.payment;

import demo.domain.Service;
import demo.event.EventService;
import demo.event.PaymentEvent;
import demo.event.PaymentEventType;
import org.springframework.util.Assert;

/**
 * The {@link PaymentService} provides transactional support for managing {@link Payment} entities. This service also
 * provides event sourcing support for {@link PaymentEvent}. Events can be appended to an {@link Payment}, which
 * contains a append-only log of actions that can be used to support remediation for distributed transactions that
 * encountered a partial failure.
 *
 * @author Kenny Bastani
 */
@org.springframework.stereotype.Service
public class PaymentService extends Service<Payment, Long> {

    private final PaymentRepository paymentRepository;
    private final EventService<PaymentEvent, Long> eventService;

    public PaymentService(PaymentRepository paymentRepository, EventService<PaymentEvent, Long> eventService) {
        this.paymentRepository = paymentRepository;
        this.eventService = eventService;
    }

    public Payment registerPayment(Payment payment) {
        payment = create(payment);

        // Trigger the payment creation event
        PaymentEvent event = payment.sendEvent(new PaymentEvent(PaymentEventType.PAYMENT_CREATED, payment));

        // Attach payment identifier
        event.getEntity()
                .setIdentity(payment.getIdentity());

        event.getEntity().getLinks().clear();

        // Return the result
        return event.getEntity();
    }

    public Payment get(Long id) {
        return paymentRepository.findOne(id);
    }

    public Payment create(Payment payment) {
        // Save the payment to the repository
        return paymentRepository.saveAndFlush(payment);
    }

    public Payment update(Payment payment) {
        Assert.notNull(payment, "Payment request body cannot be null");
        Assert.notNull(payment.getIdentity(), "Payment id must be present in the resource URL");

        Assert.state(paymentRepository.exists(payment.getIdentity()),
                "The payment with the supplied id does not exist");

        Payment currentPayment = get(payment.getIdentity());
        currentPayment.setStatus(payment.getStatus());
        currentPayment.setPaymentMethod(payment.getPaymentMethod());
        currentPayment.setOrderId(payment.getOrderId());
        currentPayment.setAmount(payment.getAmount());

        return paymentRepository.save(currentPayment);
    }

    public boolean delete(Long id) {
        Assert.state(paymentRepository.exists(id),
                "The payment with the supplied id does not exist");
        this.paymentRepository.delete(id);
        return true;
    }
}
