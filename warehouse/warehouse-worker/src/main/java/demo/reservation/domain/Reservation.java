package demo.reservation.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import demo.domain.AbstractEntity;
import demo.reservation.event.ReservationEvent;
import org.springframework.hateoas.Link;

import java.util.ArrayList;
import java.util.List;

public class Reservation extends AbstractEntity {
    private Long id;
    private ReservationStatus status;
    private List<ReservationEvent> events = new ArrayList<>();
    private String productId;
    private Long orderId;

    public Reservation() {
    }

    @JsonProperty("reservationId")
    public Long getIdentity() {
        return this.id;
    }

    public void setIdentity(Long id) {
        this.id = id;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public List<ReservationEvent> getEvents() {
        return events;
    }

    public void setEvents(List<ReservationEvent> events) {
        this.events = events;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * Returns the {@link Link} with a rel of {@link Link#REL_SELF}.
     */
    @Override
    public Link getId() {
        return getLink("self");
    }
}
