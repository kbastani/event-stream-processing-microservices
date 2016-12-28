package demo.payment;

import demo.domain.Provider;
import demo.domain.Service;

@org.springframework.stereotype.Service
public class PaymentProvider extends Provider<Payment> {

    private final PaymentService paymentService;

    public PaymentProvider(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    protected Service<? extends Payment> getDefaultService() {
        return paymentService;
    }
}
