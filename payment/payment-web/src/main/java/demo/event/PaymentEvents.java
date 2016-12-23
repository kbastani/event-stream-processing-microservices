package demo.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.payment.Payment;
import demo.payment.PaymentController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.Resources;

import java.io.Serializable;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * The {@link PaymentEvents} is a hypermedia collection of {@link PaymentEvent} resources.
 *
 * @author kbastani
 */
public class PaymentEvents extends Resources<PaymentEvent> implements Serializable {

    private Long paymentId;

    /**
     * Create a new {@link PaymentEvents} hypermedia resources collection for an {@link Payment}.
     *
     * @param paymentId is the unique identifier for the {@link Payment}
     * @param content   is the collection of {@link PaymentEvents} attached to the {@link Payment}
     */
    public PaymentEvents(Long paymentId, List<PaymentEvent> content) {
        this(content);
        this.paymentId = paymentId;

        // Add hypermedia links to resources parent
        add(linkTo(PaymentController.class)
                        .slash("payments")
                        .slash(paymentId)
                        .slash("events")
                        .withSelfRel(),
                linkTo(PaymentController.class)
                        .slash("payments")
                        .slash(paymentId)
                        .withRel("payment"));

        LinkBuilder linkBuilder = linkTo(EventController.class);

        // Add hypermedia links to each item of the collection
        content.stream().parallel().forEach(event -> event.add(
                linkBuilder.slash("events")
                        .slash(event.getEventId())
                        .withSelfRel()
        ));
    }

    /**
     * Creates a {@link Resources} instance with the given content and {@link Link}s (optional).
     *
     * @param content must not be {@literal null}.
     * @param links   the links to be added to the {@link Resources}.
     */
    private PaymentEvents(Iterable<PaymentEvent> content, Link... links) {
        super(content, links);
    }

    /**
     * Get the {@link Payment} identifier that the {@link PaymentEvents} apply to.
     *
     * @return the payment identifier
     */
    @JsonIgnore
    public Long getPaymentId() {
        return paymentId;
    }
}
