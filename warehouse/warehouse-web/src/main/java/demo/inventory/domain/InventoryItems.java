package demo.inventory.domain;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;

public class InventoryItems extends Resources<Inventory> {

    /**
     * Creates an empty {@link Resources} instance.
     */
    public InventoryItems() {
    }

    /**
     * Creates a {@link Resources} instance with the given content and {@link Link}s (optional).
     *
     * @param content must not be {@literal null}.
     * @param links   the links to be added to the {@link Resources}.
     */
    public InventoryItems(Iterable<Inventory> content, Link... links) {
        super(content, links);
    }
}
