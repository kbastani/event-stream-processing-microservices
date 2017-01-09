package demo.inventory.domain;

import demo.domain.Module;
import demo.event.EventService;
import demo.inventory.event.InventoryEvent;
import demo.inventory.event.InventoryEventService;

@org.springframework.stereotype.Service
public class InventoryModule extends Module<Inventory> {

    private final InventoryService inventoryService;
    private final InventoryEventService eventService;

    public InventoryModule(InventoryService inventoryService, InventoryEventService eventService) {
        this.inventoryService = inventoryService;
        this.eventService = eventService;
    }

    public InventoryService getInventoryService() {
        return inventoryService;
    }

    public EventService<InventoryEvent, Long> getEventService() {
        return eventService;
    }

    @Override
    public InventoryService getDefaultService() {
        return inventoryService;
    }

    @Override
    public InventoryEventService getDefaultEventService() {
        return eventService;
    }
}
