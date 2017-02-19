package demo.warehouse.event;

import demo.warehouse.domain.Warehouse;
import demo.warehouse.domain.WarehouseStatus;

/**
 * The {@link WarehouseEventType} represents a collection of possible events that describe state transitions of
 * {@link WarehouseStatus} on the {@link Warehouse} aggregate.
 *
 * @author Kenny Bastani
 */
public enum WarehouseEventType {
    WAREHOUSE_CREATED
}
