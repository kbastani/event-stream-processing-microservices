package demo.payment;

import demo.domain.BaseEntity;

public class Payment extends BaseEntity {

    private Double amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;

    public Payment() {
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

    @Override
    public String toString() {
        return "Payment{" +
                "amount=" + amount +
                ", paymentMethod=" + paymentMethod +
                ", status=" + status +
                "} " + super.toString();
    }
}
