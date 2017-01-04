package demo.payment.domain;

import demo.domain.Service;
import demo.event.EventService;
import demo.payment.event.PaymentEvent;
import demo.payment.event.PaymentEventType;
import demo.payment.repository.PaymentRepository;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;
import org.springframework.web.client.ResourceAccessException;

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

    private final Logger log = Logger.getLogger(this.getClass());
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository, EventService<PaymentEvent, Long> eventService) {
        this.paymentRepository = paymentRepository;
    }

    public Payment registerPayment(Payment payment) {
        Payment result;

        payment = create(payment);

        try {
            // Handle a synchronous event flow
            result = payment.sendEvent(new PaymentEvent(PaymentEventType.PAYMENT_CREATED, payment)).getEntity();
            result.setIdentity(payment.getIdentity());
        } catch (Exception ex) {
            log.error("Payment creation failed", ex);
            // Rollback the payment creation
            delete(payment.getIdentity());
            throw new ResourceAccessException(ex.getMessage());
        }

        // Return the result
        return result;
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

        return paymentRepository.saveAndFlush(currentPayment);
    }

    public boolean delete(Long id) {
        Assert.state(paymentRepository.exists(id),
                "The payment with the supplied id does not exist");
        this.paymentRepository.delete(id);
        return true;
    }
}
