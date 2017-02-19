package demo.reservation.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.event.Event;
import demo.reservation.controller.ReservationController;
import demo.reservation.domain.Reservation;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.hateoas.Link;

import javax.persistence.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * The domain event {@link ReservationEvent} tracks the type and state of events as applied to the {@link Reservation} domain
 * object. This event resource can be used to event source the aggregate state of {@link Reservation}.
 * <p>
 * This event resource also provides a transaction log that can be used to append actions to the event.
 *
 * @author Kenny Bastani
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(indexes = { @Index(name = "IDX_RESERVATION_EVENT", columnList = "entity_id") })
public class ReservationEvent extends Event<Reservation, ReservationEventType, Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long eventId;

    @Enumerated(EnumType.STRING)
    private ReservationEventType type;

    @OneToOne(cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    @JsonIgnore
    private Reservation entity;

    @CreatedDate
    private Long createdAt;

    @LastModifiedDate
    private Long lastModified;

    public ReservationEvent() {
    }

    public ReservationEvent(ReservationEventType type) {
        this.type = type;
    }

    public ReservationEvent(ReservationEventType type, Reservation entity) {
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
    public ReservationEventType getType() {
        return type;
    }

    @Override
    public void setType(ReservationEventType type) {
        this.type = type;
    }

    @Override
    public Reservation getEntity() {
        return entity;
    }

    @Override
    public void setEntity(Reservation entity) {
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
        return linkTo(ReservationController.class).slash("reservations").slash(getEntity().getIdentity()).slash("events")
                .slash(getEventId()).withSelfRel();
    }

    @Override
    public String toString() {
        return "ReservationEvent{" +
                "eventId=" + eventId +
                ", type=" + type +
                ", entity=" + entity +
                ", createdAt=" + createdAt +
                ", lastModified=" + lastModified +
                "} " + super.toString();
    }
}
