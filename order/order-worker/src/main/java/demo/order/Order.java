package demo.order;

import demo.address.Address;
import demo.address.AddressType;
import demo.domain.BaseEntity;
import demo.event.OrderEvent;

import java.util.HashSet;
import java.util.Set;

public class Order extends BaseEntity {

    private Long orderId;

    private OrderStatus status;

    private Set<OrderEvent> events = new HashSet<>();

    private Set<LineItem> lineItems = new HashSet<>();

    private Address shippingAddress;

    public Order() {
    }

    public Order(String accountNumber, Address shippingAddress) {
        this();
        this.shippingAddress = shippingAddress;
        if (shippingAddress.getAddressType() == null)
            this.shippingAddress.setAddressType(AddressType.SHIPPING);
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long id) {
        this.orderId = orderId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

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
                "orderId=" + orderId +
                ", status=" + status +
                ", events=" + events +
                ", lineItems=" + lineItems +
                ", shippingAddress=" + shippingAddress +
                "} " + super.toString();
    }
}
