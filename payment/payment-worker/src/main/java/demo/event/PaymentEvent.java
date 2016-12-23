package demo.event;

import demo.payment.Payment;
import demo.domain.BaseEntity;

/**
 * The domain event {@link PaymentEvent} tracks the type and state of events as
 * applied to the {@link Payment} domain object. This event resource can be used
 * to event source the aggregate state of {@link Payment}.
 * <p>
 * This event resource also provides a transaction log that can be used to append
 * actions to the event.
 *
 * @author kbastani
 */
public class PaymentEvent extends BaseEntity {

    private PaymentEventType type;

    public PaymentEvent() {
    }

    public PaymentEvent(PaymentEventType type) {
        this.type = type;
    }

    public PaymentEventType getType() {
        return type;
    }

    public void setType(PaymentEventType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "PaymentEvent{" +
                "type=" + type +
                "} " + super.toString();
    }
}
