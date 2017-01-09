package demo.warehouse.domain;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;

public class Warehouses extends Resources<Warehouse> {

    /**
     * Creates an empty {@link Resources} instance.
     */
    public Warehouses() {
    }

    /**
     * Creates a {@link Resources} instance with the given content and {@link Link}s (optional).
     *
     * @param content must not be {@literal null}.
     * @param links   the links to be added to the {@link Resources}.
     */
    public Warehouses(Iterable<Warehouse> content, Link... links) {
        super(content, links);
    }
}
