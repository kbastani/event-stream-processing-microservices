package demo.payment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.domain.BaseEntity;
import demo.event.PaymentEvent;
import org.springframework.hateoas.Link;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * The {@link Payment} domain object contains information related to
 * a user's payment. The status of an payment is event sourced using
 * events logged to the {@link PaymentEvent} collection attached to
 * this resource.
 *
 * @author kbastani
 */
@Entity
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PaymentEvent> events = new HashSet<>();

    @Enumerated(value = EnumType.STRING)
    private PaymentStatus status;

    private Double amount;
    private Long orderId;

    @Enumerated(value = EnumType.STRING)
    private PaymentMethod paymentMethod;

    public Payment() {
        status = PaymentStatus.PAYMENT_CREATED;
    }

    public Payment(Double amount, PaymentMethod paymentMethod) {
        this();
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public Long getPaymentId() {
        return id;
    }

    public void setPaymentId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    public Set<PaymentEvent> getEvents() {
        return events;
    }

    public void setEvents(Set<PaymentEvent> events) {
        this.events = events;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @JsonIgnore
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    /**
     * Returns the {@link Link} with a rel of {@link Link#REL_SELF}.
     */
    @Override
    public Link getId() {
        return linkTo(PaymentController.class)
                .slash("payments")
                .slash(getPaymentId())
                .withSelfRel();
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", events=" + events +
                ", status=" + status +
                "} " + super.toString();
    }
}
