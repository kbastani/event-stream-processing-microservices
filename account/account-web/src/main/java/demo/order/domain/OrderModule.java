package demo.order.domain;

import demo.account.event.AccountEvent;
import demo.domain.Module;
import demo.event.EventService;

@org.springframework.stereotype.Service
public class OrderModule extends Module<Order> {

    private final OrderService orderService;
    private final EventService<AccountEvent, Long> eventService;

    public OrderModule(OrderService orderService, EventService<AccountEvent, Long> eventService) {
        this.orderService = orderService;
        this.eventService = eventService;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public EventService<AccountEvent, Long> getEventService() {
        return eventService;
    }

    @Override
    public OrderService getDefaultService() {
        return orderService;
    }

    @Override
    public EventService<AccountEvent, Long> getDefaultEventService() {
        return eventService;
    }
}
