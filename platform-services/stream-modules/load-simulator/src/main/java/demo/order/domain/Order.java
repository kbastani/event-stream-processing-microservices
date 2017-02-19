package demo.order.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import demo.domain.Address;
import demo.domain.AddressType;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Order {

    private Long id;
    private Long createdAt;
    private Long lastModified;
    private List<OrderEvent> orderEvents = new ArrayList<>();
    private OrderStatus status;
    private Set<LineItem> lineItems = new HashSet<>();
    private Address shippingAddress;

    private Long accountId, paymentId;

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

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getLastModified() {
        return lastModified;
    }

    public void setLastModified(Long lastModified) {
        this.lastModified = lastModified;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
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

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    @JsonIgnore
    public List<OrderEvent> getEvents() {
        return orderEvents;
    }

    @JsonProperty("orderId")
    public Long getIdentity() {
        return id;
    }

    public void setIdentity(Long id) {
        this.id = id;
    }

    /**
     * Returns the {@link Link} with a rel of {@link Link#REL_SELF}.
     */
    public Link getId() {
        return new Link(new UriTemplate("http://order-web/v1/orders/{id}").with("id", TemplateVariable.VariableType
                .PATH_VARIABLE)
                .expand(getIdentity())
                .toString()).withSelfRel();
    }
}
