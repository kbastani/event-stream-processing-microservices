package demo.payment;

import demo.domain.Provider;
import demo.event.EventService;
import demo.event.PaymentEvent;

@org.springframework.stereotype.Service
public class PaymentProvider extends Provider<Payment> {

    private final PaymentService paymentService;
    private final EventService<PaymentEvent, Long> eventService;

    public PaymentProvider(PaymentService paymentService, EventService<PaymentEvent, Long> eventService) {
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
