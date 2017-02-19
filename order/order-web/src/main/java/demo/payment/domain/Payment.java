package demo.payment.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import demo.domain.Aggregate;
import demo.domain.Module;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;

import java.util.ArrayList;
import java.util.List;

public class Payment extends Aggregate<PaymentEvent, Long> {

    private Long id;
    private List<PaymentEvent> events = new ArrayList<>();
    private Double amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;

    public Payment() {
    }

    public Payment(Double amount, PaymentMethod paymentMethod) {
        this.amount = amount;
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @JsonProperty("paymentId")
    @Override
    public Long getIdentity() {
        return id;
    }

    public void setIdentity(Long id) {
        this.id = id;
    }

    @Override
    public List<PaymentEvent> getEvents() {
        return events;
    }

    /**
     * Returns the {@link Link} with a rel of {@link Link#REL_SELF}.
     */
    @Override
    public Link getId() {
        return new Link(new UriTemplate("http://payment-web/v1/payments/{id}")
                .with("id", TemplateVariable.VariableType
                        .PATH_VARIABLE)
                .expand(getIdentity())
                .toString()).withSelfRel();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Module<A>, A extends Aggregate<PaymentEvent, Long>> T getModule() throws
            IllegalArgumentException {
        PaymentModule paymentModule = getModule(PaymentModule.class);
        return (T) paymentModule;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "id=" + id +
                ", events=" + events +
                ", amount=" + amount +
                ", paymentMethod=" + paymentMethod +
                ", status=" + status +
                "} " + super.toString();
    }
}
