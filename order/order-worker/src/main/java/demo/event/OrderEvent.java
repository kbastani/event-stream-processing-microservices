package demo.event;

import demo.domain.BaseEntity;

public class OrderEvent extends BaseEntity {

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
        return "AccountEvent{" +
                "type=" + type +
                "} " + super.toString();
    }
}
