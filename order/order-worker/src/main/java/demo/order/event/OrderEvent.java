package demo.order.event;

import demo.domain.AbstractEntity;

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
