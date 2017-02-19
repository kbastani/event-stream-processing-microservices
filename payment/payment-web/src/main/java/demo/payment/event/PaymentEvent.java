package demo.payment.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.event.Event;
import demo.payment.controller.PaymentController;
import demo.payment.domain.Payment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.hateoas.Link;

import javax.persistence.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * The domain event {@link PaymentEvent} tracks the type and state of events as applied to the {@link Payment} domain
 * object. This event resource can be used to event source the aggregate state of {@link Payment}.
 *
 * @author kbastani
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = { @Index(name = "IDX_PAYMENT_EVENT", columnList = "entity_id") })
public class PaymentEvent extends Event<Payment, PaymentEventType, Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long eventId;

    @Enumerated(EnumType.STRING)
    private PaymentEventType type;

    @OneToOne(cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    @JsonIgnore
    private Payment entity;

    @CreatedDate
    private Long createdAt;

    @LastModifiedDate
    private Long lastModified;

    public PaymentEvent() {
    }

    public PaymentEvent(PaymentEventType type) {
        this.type = type;
    }

    public PaymentEvent(PaymentEventType type, Payment entity) {
        this.type = type;
        this.entity = entity;
    }

    @Override
    public Long getEventId() {
        return eventId;
    }

    @Override
    public void setEventId(Long id) {
        eventId = id;
    }

    @Override
    public PaymentEventType getType() {
        return type;
    }

    @Override
    public void setType(PaymentEventType type) {
        this.type = type;
    }

    @Override
    public Payment getEntity() {
        return entity;
    }

    @Override
    public void setEntity(Payment entity) {
        this.entity = entity;
    }

    @Override
    public Long getCreatedAt() {
        return createdAt;
    }

    @Override
    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public Long getLastModified() {
        return lastModified;
    }

    @Override
    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public Link getId() {
        return linkTo(PaymentController.class).slash("payments").slash(getEntity().getIdentity()).slash("events")
                .slash(getEventId()).withSelfRel();
    }
}

