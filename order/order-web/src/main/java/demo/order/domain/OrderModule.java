package demo.order.domain;

import demo.domain.Module;
import demo.event.EventService;
import demo.order.event.OrderEvent;
import demo.payment.domain.PaymentService;

@org.springframework.stereotype.Service
public class OrderModule extends Module<Order> {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final EventService<OrderEvent, Long> eventService;

    public OrderModule(OrderService orderService, PaymentService paymentService, EventService<OrderEvent, Long>
            eventService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
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

    public PaymentService getPaymentService() {
        return paymentService;
    }
}
