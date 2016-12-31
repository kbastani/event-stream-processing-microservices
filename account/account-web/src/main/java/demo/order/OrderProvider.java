package demo.order;

import demo.account.event.AccountEvent;
import demo.domain.Provider;
import demo.event.EventService;
import demo.order.domain.Order;

@org.springframework.stereotype.Service
public class OrderProvider extends Provider<Order> {

    private final OrderService orderService;
    private final EventService<AccountEvent, Long> eventService;

    public OrderProvider(OrderService orderService, EventService<AccountEvent, Long> eventService) {
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
