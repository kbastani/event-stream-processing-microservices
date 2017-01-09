package demo.order.event;

import demo.domain.AbstractEntity;
import demo.order.domain.Order;

/**
 * The domain event {@link OrderEvent} tracks the type and state of events as applied to the {@link Order} domain
 * object. This event resource can be used to event source the aggregate state of {@link Order}.
 * <p>
 * This event resource also provides a transaction log that can be used to append actions to the event.
 *
 * @author Kenny Bastani
 */
public class OrderEvent extends AbstractEntity {

    private OrderEventType type;

    public OrderEvent() {
    }

    public OrderEvent(OrderEventType type) {
        this.type = type;
    }

    public OrderEventType getType() {
        return type;
    }

    public void setType(OrderEventType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "OrderEvent{" +
                "type=" + type +
                "} " + super.toString();
    }
}
