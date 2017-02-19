package demo.inventory.event;

import demo.inventory.domain.Inventory;
import demo.inventory.domain.InventoryStatus;

/**
 * The {@link InventoryEventType} represents a collection of possible events that describe state transitions of
 * {@link InventoryStatus} on the {@link Inventory} aggregate.
 *
 * @author Kenny Bastani
 */
public enum InventoryEventType {
    INVENTORY_CREATED,
    RESERVATION_PENDING,
    RESERVATION_CONNECTED,
    INVENTORY_RESERVED,
    INVENTORY_RELEASED
}
