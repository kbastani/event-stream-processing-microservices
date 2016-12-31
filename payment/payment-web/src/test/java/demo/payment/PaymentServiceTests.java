package demo.payment;

import demo.event.EventService;
import demo.event.PaymentEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class PaymentServiceTests {

    @MockBean
    private EventService<PaymentEvent, Long> eventService;

    @MockBean
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    @Before
    public void before() {
        paymentService = new PaymentService(paymentRepository, eventService);
    }

    @Test
    public void getPaymentReturnsPayment() throws Exception {
        Payment expected = new Payment(42.0, PaymentMethod.CREDIT_CARD);

        given(this.paymentRepository.findOne(1L)).willReturn(expected);

        Payment actual = paymentService.get(1L);

        assertThat(actual).isNotNull();
        assertThat(actual.getPaymentMethod()).isEqualTo(PaymentMethod.CREDIT_CARD);
        assertThat(actual.getAmount()).isEqualTo(42.0);
    }
}