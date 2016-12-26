package demo.order;

import demo.event.ConsistencyModel;
import demo.event.EventService;
import demo.event.OrderEvent;
import demo.event.OrderEventType;
import demo.payment.Payment;
import demo.payment.PaymentMethod;
import org.apache.log4j.Logger;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = {"orders"})
public class OrderService {

    private final Logger log = Logger.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final EventService eventService;
    private final RestTemplate restTemplate;

    public OrderService(OrderRepository orderRepository, EventService eventService, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.eventService = eventService;
        this.restTemplate = restTemplate;
    }

    @CacheEvict(cacheNames = "orders", key = "#order.getOrderId().toString()")
    public Order registerOrder(Order order) {

        order = createOrder(order);

        //cacheManager.getCache("orders").evict(order.getOrderId());

        // Trigger the order creation event
        OrderEvent event = appendEvent(order.getOrderId(),
                new OrderEvent(OrderEventType.ORDER_CREATED));

        // Attach order identifier
        event.getOrder().setOrderId(order.getOrderId());

        // Return the result
        return event.getOrder();
    }

    /**
     * Create a new {@link Order} entity.
     *
     * @param order is the {@link Order} to create
     * @return the newly created {@link Order}
     */
    @CacheEvict(cacheNames = "orders", key = "#order.getOrderId().toString()")
    public Order createOrder(Order order) {

        // Save the order to the repository
        order = orderRepository.saveAndFlush(order);

        return order;
    }

    /**
     * Get an {@link Order} entity for the supplied identifier.
     *
     * @param id is the unique identifier of a {@link Order} entity
     * @return an {@link Order} entity
     */
    @Cacheable(cacheNames = "orders", key = "#id.toString()")
    public Order getOrder(Long id) {
        return orderRepository.findOne(id);
    }

    /**
     * Update an {@link Order} entity with the supplied identifier.
     *
     * @param id    is the unique identifier of the {@link Order} entity
     * @param order is the {@link Order} containing updated fields
     * @return the updated {@link Order} entity
     */
    @CachePut(cacheNames = "orders", key = "#id.toString()")
    public Order updateOrder(Long id, Order order) {
        Assert.notNull(id, "Order id must be present in the resource URL");
        Assert.notNull(order, "Order request body cannot be null");

        if (order.getOrderId() != null) {
            Assert.isTrue(Objects.equals(id, order.getOrderId()),
                    "The order id in the request body must match the resource URL");
        } else {
            order.setOrderId(id);
        }

        Assert.state(orderRepository.exists(id),
                "The order with the supplied id does not exist");

        Order currentOrder = orderRepository.findOne(id);
        currentOrder.setAccountId(order.getAccountId());
        currentOrder.setPaymentId(order.getPaymentId());
        currentOrder.setLineItems(order.getLineItems());
        currentOrder.setShippingAddress(order.getShippingAddress());
        currentOrder.setStatus(order.getStatus());

        return orderRepository.saveAndFlush(currentOrder);
    }

    /**
     * Delete the {@link Order} with the supplied identifier.
     *
     * @param id is the unique identifier for the {@link Order}
     */
    @CacheEvict(cacheNames = "orders", key = "#id.toString()")
    public Boolean deleteOrder(Long id) {
        Assert.state(orderRepository.exists(id),
                "The order with the supplied id does not exist");
        this.orderRepository.delete(id);
        return true;
    }

    /**
     * Append a new {@link OrderEvent} to the {@link Order} reference for the supplied identifier.
     *
     * @param orderId is the unique identifier for the {@link Order}
     * @param event   is the {@link OrderEvent} to append to the {@link Order} entity
     * @param links   is the optional {@link Link} to embed in the {@link org.springframework.hateoas.Resource}
     * @return the newly appended {@link OrderEvent}
     */
    public OrderEvent appendEvent(Long orderId, OrderEvent event, Link... links) {
        return appendEvent(orderId, event, ConsistencyModel.ACID, links);
    }

    /**
     * Append a new {@link OrderEvent} to the {@link Order} reference for the supplied identifier.
     *
     * @param orderId is the unique identifier for the {@link Order}
     * @param event   is the {@link OrderEvent} to append to the {@link Order} entity
     * @return the newly appended {@link OrderEvent}
     */
    public OrderEvent appendEvent(Long orderId, OrderEvent event) {
        return appendEvent(orderId, event, ConsistencyModel.ACID);
    }

    /**
     * Append a new {@link OrderEvent} to the {@link Order} reference for the supplied identifier.
     *
     * @param orderId is the unique identifier for the {@link Order}
     * @param event   is the {@link OrderEvent} to append to the {@link Order} entity
     * @return the newly appended {@link OrderEvent}
     */
    public OrderEvent appendEvent(Long orderId, OrderEvent event, ConsistencyModel consistencyModel, Link... links) {
        Order order = getOrder(orderId);
        Assert.notNull(order, "The order with the supplied id does not exist");
        event.setOrder(order);
        event = eventService.createEvent(orderId, event);
        order.getEvents().add(event);
        order = orderRepository.saveAndFlush(order);
        event.setOrder(order);
        eventService.raiseEvent(event, consistencyModel, links);
        return event;
    }

    /**
     * Apply an {@link OrderCommand} to the {@link Order} with a specified identifier.
     *
     * @param id           is the unique identifier of the {@link Order}
     * @param orderCommand is the command to apply to the {@link Order}
     * @return a hypermedia resource containing the updated {@link Order}
     */
    @CachePut(cacheNames = "orders", key = "#id.toString()")
    public Order applyCommand(Long id, OrderCommand orderCommand) {
        Order order = getOrder(id);

        Assert.notNull(order, "The order for the supplied id could not be found");

        OrderStatus status = order.getStatus();

        // TODO: Implement apply command

        return order;
    }

    public Order connectAccount(Long id, Long accountId) {
        // Get the order
        Order order = getOrder(id);

        // Connect the account
        order.setAccountId(accountId);
        order.setStatus(OrderStatus.ACCOUNT_CONNECTED);
        order = updateOrder(id, order);

        //cacheManager.getCache("orders").evict(id);

        // Trigger the account connected event
        OrderEvent event = appendEvent(order.getOrderId(),
                new OrderEvent(OrderEventType.ACCOUNT_CONNECTED));

        // Set non-serializable fields
        event.getOrder().setAccountId(order.getAccountId());
        event.getOrder().setPaymentId(order.getPaymentId());
        event.getOrder().setOrderId(order.getOrderId());

        // Return the result
        return event.getOrder();
    }

    public Order connectPayment(Long id, Long paymentId) {
        // Get the order
        Order order = getOrder(id);

        // Connect the account
        order.setPaymentId(paymentId);
        order.setStatus(OrderStatus.PAYMENT_CONNECTED);
        order = updateOrder(id, order);

        // cacheManager.getCache("orders").evict(id);

        // Trigger the account connected event
        OrderEvent event = appendEvent(order.getOrderId(),
                new OrderEvent(OrderEventType.PAYMENT_CONNECTED));

        // Set non-serializable fields
        event.getOrder().setAccountId(order.getAccountId());
        event.getOrder().setPaymentId(order.getPaymentId());
        event.getOrder().setOrderId(order.getOrderId());

        // Return the result
        return event.getOrder();
    }

    public Order createPayment(Long id) {
        // Get the order
        Order order = getOrder(id);

        Payment payment = new Payment();

        // Calculate payment amount
        payment.setAmount(order.getLineItems()
                .stream()
                .mapToDouble(a -> (a.getPrice() + a.getTax()) * a.getQuantity())
                .sum());

        // Set payment method
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        // Create a new request entity
        RequestEntity<Resource<Payment>> requestEntity = RequestEntity.post(
                URI.create("http://localhost:8082/v1/payments"))
                .contentType(MediaTypes.HAL_JSON)
                .body(new Resource<Payment>(payment), Resource.class);

        // Update the order entity's status
        payment = restTemplate.exchange(requestEntity, Payment.class)
                .getBody();

        log.info(payment);

        // Update the status
        order.setStatus(OrderStatus.PAYMENT_CREATED);
        order = updateOrder(id, order);

        // cacheManager.getCache("orders").evict(id);

        // Trigger the account connected event
        OrderEvent event = appendEvent(order.getOrderId(),
                new OrderEvent(OrderEventType.PAYMENT_CREATED),
                new Link(payment.getId().getHref(), "payment"));

        // Set non-serializable fields
        event.getOrder()
                .setAccountId(Optional.ofNullable(event.getOrder().getAccountId())
                        .orElse(order.getAccountId()));

        event.getOrder()
                .setPaymentId(Optional.ofNullable(event.getOrder().getPaymentId())
                        .orElse(order.getPaymentId()));

        event.getOrder().setOrderId(order.getOrderId());

        // Return the result
        return event.getOrder();
    }
}
