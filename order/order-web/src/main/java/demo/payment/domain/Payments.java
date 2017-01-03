package demo.payment.domain;

import demo.order.domain.Order;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;

public class Payments extends Resources<Order> {

    /**
     * Creates an empty {@link Resources} instance.
     */
    public Payments() {
    }

    /**
     * Creates a {@link Resources} instance with the given content and {@link Link}s (optional).
     *
     * @param content must not be {@literal null}.
     * @param links   the links to be added to the {@link Resources}.
     */
    public Payments(Iterable<Order> content, Link... links) {
        super(content, links);
    }
}
