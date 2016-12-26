package demo.order;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.address.Address;
import demo.address.AddressType;
import demo.domain.BaseEntity;
import demo.event.OrderEvent;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private Long accountId;
    private Long paymentId;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<OrderEvent> events = new HashSet<>();

    @Enumerated(value = EnumType.STRING)
    private OrderStatus status;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<LineItem> lineItems = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL)
    private Address shippingAddress;

    public Order() {
        this.status = OrderStatus.ORDER_CREATED;
    }

    public Order(Long accountId, Address shippingAddress) {
        this();
        this.accountId = accountId;
        this.shippingAddress = shippingAddress;
        if (shippingAddress.getAddressType() == null)
            this.shippingAddress.setAddressType(AddressType.SHIPPING);
    }

    @JsonIgnore
    public Long getOrderId() {
        return id;
    }

    public void setOrderId(Long id) {
        this.id = id;
    }

    @JsonIgnore
    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    @JsonIgnore
    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    @JsonIgnore
    public Set<OrderEvent> getEvents() {
        return events;
    }

    public void setEvents(Set<OrderEvent> events) {
        this.events = events;
    }

    public Set<LineItem> getLineItems() {
        return lineItems;
    }

    public void setLineItems(Set<LineItem> lineItems) {
        this.lineItems = lineItems;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public void addLineItem(LineItem lineItem) {
        lineItems.add(lineItem);
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", accountId=" + accountId +
                ", paymentId=" + paymentId +
                ", status=" + status +
                ", lineItems=" + lineItems +
                ", shippingAddress=" + shippingAddress +
                "} " + super.toString();
    }
}
