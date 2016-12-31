package demo.order;

import demo.domain.Provider;
import demo.event.EventService;
import demo.event.OrderEvent;

@org.springframework.stereotype.Service
public class OrderProvider extends Provider<Order> {

    private final OrderService orderService;
    private final EventService<OrderEvent, Long> eventService;

    public OrderProvider(OrderService orderService, EventService<OrderEvent, Long> eventService) {
        this.orderService = orderService;
        this.eventService = eventService;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public EventService<OrderEvent, Long> getEventService() {
        return eventService;
    }

    @Override
    public OrderService getDefaultService() {
        return orderService;
    }

    @Override
    public EventService<OrderEvent, Long> getDefaultEventService() {
        return eventService;
    }
}
