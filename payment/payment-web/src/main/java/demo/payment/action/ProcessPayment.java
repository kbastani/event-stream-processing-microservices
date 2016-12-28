package demo.payment.action;

import demo.domain.Action;
import demo.payment.Payment;
import demo.payment.PaymentStatus;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

@Service
public class ProcessPayment extends Action<Payment> {
    public Consumer<Payment> getConsumer() {
        return payment -> payment.setStatus(PaymentStatus.PAYMENT_PROCESSED);
    }
}
