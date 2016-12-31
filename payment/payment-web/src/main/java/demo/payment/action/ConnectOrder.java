package demo.payment.action;

import demo.domain.Action;
import demo.payment.domain.Payment;
import demo.payment.domain.PaymentModule;
import demo.payment.domain.PaymentService;
import demo.payment.domain.PaymentStatus;
import demo.payment.event.PaymentEvent;
import demo.payment.event.PaymentEventType;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

@Service
public class ConnectOrder extends Action<Payment> {
    public BiConsumer<Payment, Long> getConsumer() {
        return (payment, orderId) -> {
            PaymentService paymentService = payment.getModule(PaymentModule.class)
                    .getDefaultService();

            // Connect the payment to the order
            payment.setOrderId(orderId);
            payment.setStatus(PaymentStatus.ORDER_CONNECTED);
            payment = paymentService.update(payment);

            // Trigger the payment connected
            payment.sendAsyncEvent(new PaymentEvent(PaymentEventType.ORDER_CONNECTED, payment));
        };
    }
}
