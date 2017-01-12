package demo.function;

import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import demo.order.domain.Order;
import demo.order.domain.OrderStatus;
import org.apache.log4j.Logger;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.RequestEntity;
import org.springframework.statemachine.StateContext;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.function.Function;

public class PaymentSucceeded extends OrderFunction {

    final private Logger log = Logger.getLogger(PaymentSucceeded.class);

    public PaymentSucceeded(StateContext<OrderStatus, OrderEventType> context) {
        this(context, null);
    }

    public PaymentSucceeded(StateContext<OrderStatus, OrderEventType> context, Function<OrderEvent, Order> lambda) {
        super(context, lambda);
    }

    /**
     * Apply an {@link OrderEvent} to the lambda function that was provided through the
     * constructor of this {@link OrderFunction}.
     *
     * @param event is the {@link OrderEvent} to apply to the lambda function
     */
    @Override
    public Order apply(OrderEvent event) {
        Order order;

        log.info("Executing workflow for payment succeeded...");

        // Create a traverson for the root order
        Traverson traverson = new Traverson(
                URI.create(event.getLink("order").getHref()),
                MediaTypes.HAL_JSON
        );

        // Get the order resource attached to the event
        order = traverson.follow("self")
                .toEntity(Order.class)
                .getBody();

        // Set the order to a pending state
        order = setOrderPaymentSucceededStatus(event, order);

        if (order.getStatus() == OrderStatus.PAYMENT_SUCCEEDED) {
            order = traverson.follow("self", "commands", "completeOrder")
                    .toObject(Order.class);
        }

        context.getExtendedState().getVariables().put("order", order);

        return order;
    }

    /**
     * Set the {@link Order} resource to a payment succeeded state.
     *
     * @param event is the {@link OrderEvent} for this context
     * @param order is the {@link Order} attached to the {@link OrderEvent} resource
     * @return an {@link Order} with its updated state set to pending
     */
    private Order setOrderPaymentSucceededStatus(OrderEvent event, Order order) {
        // Set the account status to pending
        order.setStatus(OrderStatus.PAYMENT_SUCCEEDED);
        RestTemplate restTemplate = new RestTemplate();

        // Create a new request entity
        RequestEntity<Order> requestEntity = RequestEntity.put(
                URI.create(event.getLink("order").getHref()))
                .contentType(MediaTypes.HAL_JSON)
                .body(order);

        // Update the account entity's status
        order = restTemplate.exchange(requestEntity, Order.class).getBody();

        return order;
    }
}
