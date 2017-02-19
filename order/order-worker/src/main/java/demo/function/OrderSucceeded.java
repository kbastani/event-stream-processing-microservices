package demo.function;

import demo.order.domain.Order;
import demo.order.domain.OrderStatus;
import demo.order.event.OrderEvent;
import demo.order.event.OrderEventType;
import org.apache.log4j.Logger;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.statemachine.StateContext;

import java.net.URI;
import java.util.function.Function;

public class OrderSucceeded extends OrderFunction {

    final private Logger log = Logger.getLogger(OrderSucceeded.class);

    public OrderSucceeded(StateContext<OrderStatus, OrderEventType> context) {
        this(context, null);
    }

    public OrderSucceeded(StateContext<OrderStatus, OrderEventType> context, Function<OrderEvent, Order> lambda) {
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

        log.info("Executing workflow for order succeeded...");

        // Create a traverson for the root order
        Traverson traverson = new Traverson(
                URI.create(event.getLink("order").getHref()),
                MediaTypes.HAL_JSON
        );

        order = traverson.follow("self").toObject(Order.class);

        context.getExtendedState().getVariables().put("order", order);

        return order;
    }
}
