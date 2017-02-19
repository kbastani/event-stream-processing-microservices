package demo.warehouse.domain;

import demo.domain.Module;
import demo.event.EventService;
import demo.order.event.OrderEvent;

@org.springframework.stereotype.Service
public class WarehouseModule extends Module<Warehouse> {

    private final WarehouseService warehouseService;
    private final EventService<OrderEvent, Long> eventService;

    public WarehouseModule(WarehouseService warehouseService, EventService<OrderEvent, Long> eventService) {
        this.warehouseService = warehouseService;
        this.eventService = eventService;
    }

    public WarehouseService getWarehouseService() {
        return warehouseService;
    }

    public EventService<OrderEvent, Long> getEventService() {
        return eventService;
    }

    @Override
    public WarehouseService getDefaultService() {
        return warehouseService;
    }

    @Override
    public EventService<OrderEvent, Long> getDefaultEventService() {
        return eventService;
    }
}
