package demo.warehouse.event;

import demo.domain.AbstractEntity;
import demo.warehouse.domain.Warehouse;

/**
 * The domain event {@link WarehouseEvent} tracks the type and state of events as applied to the {@link Warehouse} domain
 * object. This event resource can be used to event source the aggregate state of {@link Warehouse}.
 *
 * @author kbastani
 */
public class WarehouseEvent extends AbstractEntity {

    private WarehouseEventType type;

    public WarehouseEvent() {
    }

    public WarehouseEvent(WarehouseEventType type) {
        this.type = type;
    }

    public WarehouseEventType getType() {
        return type;
    }

    public void setType(WarehouseEventType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "WarehouseEvent{" +
                "type=" + type +
                "} " + super.toString();
    }
}
