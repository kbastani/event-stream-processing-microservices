package demo.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.payment.Payment;
import demo.domain.BaseEntity;

import javax.persistence.*;

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
@Entity
public class PaymentEvent extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private PaymentEventType type;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Payment payment;

    public PaymentEvent() {
    }

    public PaymentEvent(PaymentEventType type) {
        this.type = type;
    }

    @JsonIgnore
    public Long getEventId() {
        return id;
    }

    public void setEventId(Long id) {
        this.id = id;
    }

    public PaymentEventType getType() {
        return type;
    }

    public void setType(PaymentEventType type) {
        this.type = type;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    @Override
    public String toString() {
        return "PaymentEvent{" +
                "id=" + id +
                ", type=" + type +
                ", payment=" + payment +
                "} " + super.toString();
    }
}
