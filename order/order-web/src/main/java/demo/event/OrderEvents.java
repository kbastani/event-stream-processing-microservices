package demo.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.order.Order;
import demo.order.OrderController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.Resources;

import java.io.Serializable;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * The {@link OrderEvents} is a hypermedia collection of {@link OrderEvent} resources.
 *
 * @author kbastani
 */
public class OrderEvents extends Resources<OrderEvent> implements Serializable {

    private Long orderId;

    /**
     * Create a new {@link OrderEvents} hypermedia resources collection for an {@link Order}.
     *
     * @param orderId is the unique identifier for the {@link Order}
     * @param content   is the collection of {@link OrderEvents} attached to the {@link Order}
     */
    public OrderEvents(Long orderId, List<OrderEvent> content) {
        this(content);
        this.orderId = orderId;

        // Add hypermedia links to resources parent
        add(linkTo(OrderController.class)
                        .slash("orders")
                        .slash(orderId)
                        .slash("events")
                        .withSelfRel(),
                linkTo(OrderController.class)
                        .slash("orders")
                        .slash(orderId)
                        .withRel("order"));

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
    private OrderEvents(Iterable<OrderEvent> content, Link... links) {
        super(content, links);
    }

    /**
     * Get the {@link Order} identifier that the {@link OrderEvents} apply to.
     *
     * @return the order identifier
     */
    @JsonIgnore
    public Long getOrderId() {
        return orderId;
    }
}
