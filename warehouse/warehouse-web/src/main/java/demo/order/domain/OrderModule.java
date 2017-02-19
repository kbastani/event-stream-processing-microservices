package demo.order.domain;

import demo.warehouse.event.WarehouseEvent;
import demo.domain.Module;
import demo.event.EventService;

@org.springframework.stereotype.Service
public class OrderModule extends Module<Order> {

    private final OrderService orderService;
    private final EventService<WarehouseEvent, Long> eventService;

    public OrderModule(OrderService orderService, EventService<WarehouseEvent, Long> eventService) {
        this.orderService = orderService;
        this.eventService = eventService;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public EventService<WarehouseEvent, Long> getEventService() {
        return eventService;
    }

    @Override
    public OrderService getDefaultService() {
        return orderService;
    }

    @Override
    public EventService<WarehouseEvent, Long> getDefaultEventService() {
        return eventService;
    }
}
