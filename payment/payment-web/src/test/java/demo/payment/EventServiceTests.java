package demo.payment;

import demo.event.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class EventServiceTests {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EventService<PaymentEvent, Long> eventService;

    @Test
    public void getPaymentReturnsPayment() throws Exception {
        Payment payment = new Payment(11.0, PaymentMethod.CREDIT_CARD);
        payment = paymentRepository.saveAndFlush(payment);
        eventService.save(new PaymentEvent(PaymentEventType.PAYMENT_CREATED, payment));
        Events events = eventService.find(payment.getIdentity());
        Assert.notNull(events);
    }
}