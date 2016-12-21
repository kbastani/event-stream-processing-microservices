package demo.order;

import demo.event.ConsistencyModel;
import demo.event.EventService;
import demo.event.OrderEvent;
import demo.event.OrderEventType;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Objects;

@Service
@CacheConfig(cacheNames = {"orders"})
public class OrderService {

    private final OrderRepository orderRepository;
    private final EventService eventService;
    private final CacheManager cacheManager;

    public OrderService(OrderRepository orderRepository, EventService eventService, CacheManager cacheManager) {
        this.orderRepository = orderRepository;
        this.eventService = eventService;
        this.cacheManager = cacheManager;
    }

    @CacheEvict(cacheNames = "orders", key = "#order.getOrderId().toString()")
    public Order registerOrder(Order order) {

        order = createOrder(order);

        cacheManager.getCache("orders")
                .evict(order.getOrderId());

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
     * @param id      is the unique identifier of the {@link Order} entity
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
        currentOrder.setAccountNumber(order.getAccountNumber());
        currentOrder.setLineItems(order.getLineItems());
        currentOrder.setShippingAddress(order.getShippingAddress());
        currentOrder.setStatus(order.getStatus());

        return orderRepository.save(currentOrder);
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
     * @param event     is the {@link OrderEvent} to append to the {@link Order} entity
     * @return the newly appended {@link OrderEvent}
     */
    public OrderEvent appendEvent(Long orderId, OrderEvent event) {
        return appendEvent(orderId, event, ConsistencyModel.ACID);
    }

    /**
     * Append a new {@link OrderEvent} to the {@link Order} reference for the supplied identifier.
     *
     * @param orderId is the unique identifier for the {@link Order}
     * @param event     is the {@link OrderEvent} to append to the {@link Order} entity
     * @return the newly appended {@link OrderEvent}
     */
    public OrderEvent appendEvent(Long orderId, OrderEvent event, ConsistencyModel consistencyModel) {
        Order order = getOrder(orderId);
        Assert.notNull(order, "The order with the supplied id does not exist");
        event.setOrder(order);
        event = eventService.createEvent(orderId, event);
        order.getEvents().add(event);
        orderRepository.saveAndFlush(order);
        eventService.raiseEvent(event, consistencyModel);
        return event;
    }

    /**
     * Apply an {@link OrderCommand} to the {@link Order} with a specified identifier.
     *
     * @param id             is the unique identifier of the {@link Order}
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
}
