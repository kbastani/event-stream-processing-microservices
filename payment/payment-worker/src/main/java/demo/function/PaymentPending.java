package demo.function;

import demo.event.PaymentEvent;
import demo.event.PaymentEventType;
import demo.payment.Payment;
import demo.payment.PaymentStatus;
import org.apache.log4j.Logger;
import org.springframework.statemachine.StateContext;

import java.util.function.Function;

public class PaymentPending extends PaymentFunction {

    final private Logger log = Logger.getLogger(PaymentPending.class);

    public PaymentPending(StateContext<PaymentStatus, PaymentEventType> context, Function<PaymentEvent, Payment> lambda) {
        super(context, lambda);
    }

    /**
     * Apply an {@link PaymentEvent} to the lambda function that was provided through the
     * constructor of this {@link PaymentFunction}.
     *
     * @param event is the {@link PaymentEvent} to apply to the lambda function
     */
    @Override
    public Payment apply(PaymentEvent event) {
        log.info("Executing workflow for payment pending...");
        return super.apply(event);
    }
}
