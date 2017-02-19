package demo.payment.domain;

import demo.domain.Module;
import demo.event.EventService;
import demo.payment.event.PaymentEvent;

@org.springframework.stereotype.Service
public class PaymentModule extends Module<Payment> {

    private final PaymentService paymentService;
    private final EventService<PaymentEvent, Long> eventService;

    public PaymentModule(PaymentService paymentService, EventService<PaymentEvent, Long> eventService) {
        this.paymentService = paymentService;
        this.eventService = eventService;
    }

    @Override
    public PaymentService getDefaultService() {
        return paymentService;
    }

    @Override
    public EventService<PaymentEvent, Long> getDefaultEventService() {
        return eventService;
    }
}
