package demo.reservation.domain;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;

public class Reservations extends Resources<Reservation> {

    /**
     * Creates an empty {@link Resources} instance.
     */
    public Reservations() {
    }

    /**
     * Creates a {@link Resources} instance with the given content and {@link Link}s (optional).
     *
     * @param content must not be {@literal null}.
     * @param links   the links to be added to the {@link Resources}.
     */
    public Reservations(Iterable<Reservation> content, Link... links) {
        super(content, links);
    }
}
