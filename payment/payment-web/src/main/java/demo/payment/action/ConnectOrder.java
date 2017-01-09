package demo.payment.action;

import demo.domain.Action;
import demo.payment.domain.Payment;
import demo.payment.domain.PaymentModule;
import demo.payment.domain.PaymentService;
import demo.payment.domain.PaymentStatus;
import demo.payment.event.PaymentEvent;
import demo.payment.event.PaymentEventType;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.function.BiFunction;

@Service
public class ConnectOrder extends Action<Payment> {
    private final Logger log = Logger.getLogger(this.getClass());

    public BiFunction<Payment, Long, Payment> getFunction() {
        return (payment, orderId) -> {
            Assert.isTrue(payment
                    .getStatus() == PaymentStatus.PAYMENT_CREATED, "Payment has already been connected to an order");

            PaymentService paymentService = payment.getModule(PaymentModule.class)
                    .getDefaultService();

            // Connect the payment to the order
            payment.setOrderId(orderId);
            payment.setStatus(PaymentStatus.ORDER_CONNECTED);
            payment = paymentService.update(payment);

            try {
                // Trigger the payment connected
                payment.sendAsyncEvent(new PaymentEvent(PaymentEventType.ORDER_CONNECTED, payment));
            } catch (IllegalStateException ex) {
                log.error("Payment could not be connected to order", ex);

                // Rollback operation
                payment.setStatus(PaymentStatus.PAYMENT_CREATED);
                payment.setOrderId(null);
                paymentService.update(payment);

                throw ex;
            }

            return payment;
        };
    }
}
