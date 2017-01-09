package demo.warehouse.domain;

import demo.domain.Module;
import demo.warehouse.event.WarehouseEventService;

@org.springframework.stereotype.Service
public class WarehouseModule extends Module<Warehouse> {

    private final WarehouseService warehouseService;
    private final WarehouseEventService eventService;

    public WarehouseModule(WarehouseService warehouseService, WarehouseEventService eventService) {
        this.warehouseService = warehouseService;
        this.eventService = eventService;
    }

    public WarehouseService getWarehouseService() {
        return warehouseService;
    }

    public WarehouseEventService getEventService() {
        return eventService;
    }

    @Override
    public WarehouseService getDefaultService() {
        return warehouseService;
    }

    @Override
    public WarehouseEventService getDefaultEventService() {
        return eventService;
    }
}
