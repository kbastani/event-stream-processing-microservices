package demo.reservation.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import demo.domain.Aggregate;
import demo.domain.Module;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;

import java.util.ArrayList;
import java.util.List;

public class Reservation extends Aggregate<ReservationEvent, Long> {
    private Long id;
    private ReservationStatus status;
    private List<ReservationEvent> events = new ArrayList<>();
    private String productId;
    private Long orderId;

    public Reservation() {
    }

    @JsonProperty("reservationId")
    @Override
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

    @Override
    public List<ReservationEvent> getEvents() {
        return events;
    }

    /**
     * Returns the {@link Link} with a rel of {@link Link#REL_SELF}.
     */
    @Override
    public Link getId() {
        return new Link(new UriTemplate("http://warehouse-web/v1/reservations/{id}")
                .with("id", TemplateVariable.VariableType
                        .PATH_VARIABLE)
                .expand(getIdentity())
                .toString()).withSelfRel();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Module<A>, A extends Aggregate<ReservationEvent, Long>> T getModule() throws
            IllegalArgumentException {
        ReservationModule reservationModule = getModule(ReservationModule.class);
        return (T) reservationModule;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", status=" + status +
                ", events=" + events +
                ", productId='" + productId + '\'' +
                ", orderId=" + orderId +
                "} " + super.toString();
    }
}
