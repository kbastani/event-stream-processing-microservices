package demo.payment.action;

import demo.domain.Action;
import demo.payment.domain.Payment;
import demo.payment.domain.PaymentModule;
import demo.payment.domain.PaymentService;
import demo.payment.domain.PaymentStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.function.Consumer;

@Service
public class ProcessPayment extends Action<Payment> {
    public Consumer<Payment> getConsumer() {
        return payment -> {
            // Validations
            Assert.isTrue(!Arrays.asList(PaymentStatus.PAYMENT_SUCCEEDED,
                    PaymentStatus.PAYMENT_PENDING,
                    PaymentStatus.PAYMENT_FAILED).contains(payment.getStatus()), "Payment has already been processed");
            Assert.isTrue(payment.getStatus() == PaymentStatus.ORDER_CONNECTED,
                    "Payment must be connected to an order");

            PaymentService paymentService = payment.getModule(PaymentModule.class)
                    .getDefaultService();

            payment.setStatus(PaymentStatus.PAYMENT_PROCESSED);
            paymentService.update(payment);
        };
    }
}
