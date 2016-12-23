package demo.function;

import demo.order.Order;
import demo.order.OrderStatus;
import demo.event.OrderEvent;
import demo.event.OrderEventType;
import org.apache.log4j.Logger;
import org.springframework.statemachine.StateContext;

import java.util.function.Function;

/**
 * The {@link OrderFunction} is an abstraction used to map actions that are triggered by
 * state transitions on a {@link demo.order.Order} resource on to a function. Mapped functions
 * can take multiple forms and reside either remotely or locally on the classpath of this application.
 *
 * @author kbastani
 */
public abstract class OrderFunction {

    final private Logger log = Logger.getLogger(OrderFunction.class);
    final protected StateContext<OrderStatus, OrderEventType> context;
    final protected Function<OrderEvent, Order> lambda;

    /**
     * Create a new instance of a class that extends {@link OrderFunction}, supplying
     * a state context and a lambda function used to apply {@link OrderEvent} to a provided
     * action.
     *
     * @param context is the {@link StateContext} for a replicated state machine
     * @param lambda  is the lambda function describing an action that consumes an {@link OrderEvent}
     */
    public OrderFunction(StateContext<OrderStatus, OrderEventType> context,
                         Function<OrderEvent, Order> lambda) {
        this.context = context;
        this.lambda = lambda;
    }

    /**
     * Apply an {@link OrderEvent} to the lambda function that was provided through the
     * constructor of this {@link OrderFunction}.
     *
     * @param event is the {@link OrderEvent} to apply to the lambda function
     */
    public Order apply(OrderEvent event) {
        // Execute the lambda function
        Order result = lambda.apply(event);
        context.getExtendedState().getVariables().put("order", result);
        log.info("Order function: " + event.getType());
        return result;
    }
}
