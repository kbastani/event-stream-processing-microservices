package demo.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.domain.BaseEntity;
import demo.order.Order;

import javax.persistence.*;

/**
 * The domain event {@link OrderEvent} tracks the type and state of events as
 * applied to the {@link Order} domain object. This event resource can be used
 * to event source the aggregate state of {@link Order}.
 * <p>
 * This event resource also provides a transaction log that can be used to append
 * actions to the event.
 *
 * @author kbastani
 */
@Entity
public class OrderEvent extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderEventType type;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Order order;

    public OrderEvent() {
    }

    public OrderEvent(OrderEventType type) {
        this.type = type;
    }

    @JsonIgnore
    public Long getEventId() {
        return id;
    }

    public void setEventId(Long id) {
        this.id = id;
    }

    public OrderEventType getType() {
        return type;
    }

    public void setType(OrderEventType type) {
        this.type = type;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    @Override
    public String toString() {
        return "OrderEvent{" +
                "id=" + id +
                ", type=" + type +
                ", order=" + order +
                "} " + super.toString();
    }
}
