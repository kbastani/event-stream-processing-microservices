package demo.inventory.event;

import demo.event.EventRepository;

public interface InventoryEventRepository extends EventRepository<InventoryEvent, Long> {
}
