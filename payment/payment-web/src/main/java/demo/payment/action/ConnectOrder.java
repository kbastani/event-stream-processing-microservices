package demo.payment.action;

import demo.domain.Action;
import demo.payment.Payment;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

@Service
public class ConnectOrder extends Action<Payment> {
    public BiConsumer<Payment, Long> getConsumer() {
        return Payment::setOrderId;
    }
}
