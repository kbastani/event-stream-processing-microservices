package demo.payment;

import demo.event.*;
import demo.payment.domain.Payment;
import demo.payment.domain.PaymentMethod;
import demo.payment.event.PaymentEvent;
import demo.payment.event.PaymentEventType;
import demo.payment.repository.PaymentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
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

    @MockBean
    private DiscoveryClient discoveryClient;

    @Test
    public void getPaymentReturnsPayment() throws Exception {
        Payment payment = new Payment(11.0, PaymentMethod.CREDIT_CARD);
        payment = paymentRepository.saveAndFlush(payment);
        eventService.save(new PaymentEvent(PaymentEventType.PAYMENT_CREATED, payment));
        Events events = eventService.find(payment.getIdentity());
        Assert.notNull(events);
    }
}