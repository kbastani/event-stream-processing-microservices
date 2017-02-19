package demo.inventory.domain;

import demo.domain.AbstractEntity;

/**
 * The domain event {@link InventoryEvent} tracks the type and state of events as applied to the {@link Inventory} domain
 * object. This event resource can be used to event source the aggregate state of {@link Inventory}.
 *
 * @author kbastani
 */
public class InventoryEvent extends AbstractEntity {

    private InventoryEventType type;

    public InventoryEvent() {
    }

    public InventoryEvent(InventoryEventType type) {
        this.type = type;
    }

    public InventoryEventType getType() {
        return type;
    }

    public void setType(InventoryEventType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "InventoryEvent{" +
                "type=" + type +
                "} " + super.toString();
    }
}
