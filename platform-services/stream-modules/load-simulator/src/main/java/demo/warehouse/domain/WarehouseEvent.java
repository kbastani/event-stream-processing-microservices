package demo.warehouse.domain;

/**
 * The domain event {@link WarehouseEvent} tracks the type and state of events as applied to the {@link Warehouse}
 * domain
 * object. This event resource can be used to event source the aggregate state of {@link Warehouse}.
 *
 * @author kbastani
 */
public class WarehouseEvent {

    private Long eventId;
    private WarehouseEventType type;
    private Warehouse entity;
    private Long createdAt;
    private Long lastModified;

    public WarehouseEvent() {
    }

    public WarehouseEvent(WarehouseEventType type) {
        this.type = type;
    }

    public WarehouseEvent(WarehouseEventType type, Warehouse entity) {
        this.type = type;
        this.entity = entity;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long id) {
        eventId = id;
    }

    public WarehouseEventType getType() {
        return type;
    }

    public void setType(WarehouseEventType type) {
        this.type = type;
    }

    public Warehouse getEntity() {
        return entity;
    }

    public void setEntity(Warehouse entity) {
        this.entity = entity;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public String toString() {
        return "WarehouseEvent{" +
                "eventId=" + eventId +
                ", type=" + type +
                ", entity=" + entity +
                ", createdAt=" + createdAt +
                ", lastModified=" + lastModified +
                "} " + super.toString();
    }
}
