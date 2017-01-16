package demo.order.action;

import demo.domain.Action;
import demo.order.domain.Order;
import demo.order.domain.OrderModule;
import demo.payment.domain.Payment;
import demo.payment.domain.PaymentService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Processes a {@link Payment} for an {@link Order}.
 *
 * @author Kenny Bastani
 */
@Service
@Transactional
public class DeleteOrder extends Action<Order> {

    private final Logger log = Logger.getLogger(this.getClass());
    private final PaymentService paymentService;

    public DeleteOrder(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public void apply(Order order) {
        // Delete payment
        if (order.getPaymentId() != null)
            paymentService.delete(order.getPaymentId());

        // Delete order
        order.getModule(OrderModule.class)
                .getDefaultService()
                .delete(order.getIdentity());
    }
}
