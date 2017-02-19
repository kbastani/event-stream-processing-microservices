package demo.warehouse.function;

import demo.warehouse.domain.Warehouse;
import demo.warehouse.domain.WarehouseStatus;
import demo.warehouse.event.WarehouseEvent;
import demo.warehouse.event.WarehouseEventType;
import org.apache.log4j.Logger;
import org.springframework.statemachine.StateContext;

import java.util.function.Function;

/**
 * The {@link WarehouseFunction} is an abstraction used to map actions that are triggered by
 * state transitions on a {@link Warehouse} resource on to a function. Mapped functions
 * can take multiple forms and reside either remotely or locally on the classpath of this application.
 *
 * @author kbastani
 */
public abstract class WarehouseFunction {

    final private Logger log = Logger.getLogger(WarehouseFunction.class);
    final protected StateContext<WarehouseStatus, WarehouseEventType> context;
    final protected Function<WarehouseEvent, Warehouse> lambda;

    /**
     * Create a new instance of a class that extends {@link WarehouseFunction}, supplying
     * a state context and a lambda function used to apply {@link WarehouseEvent} to a provided
     * action.
     *
     * @param context is the {@link StateContext} for a replicated state machine
     * @param lambda  is the lambda function describing an action that consumes an {@link WarehouseEvent}
     */
    public WarehouseFunction(StateContext<WarehouseStatus, WarehouseEventType> context,
                         Function<WarehouseEvent, Warehouse> lambda) {
        this.context = context;
        this.lambda = lambda;
    }

    /**
     * Apply an {@link WarehouseEvent} to the lambda function that was provided through the
     * constructor of this {@link WarehouseFunction}.
     *
     * @param event is the {@link WarehouseEvent} to apply to the lambda function
     */
    public Warehouse apply(WarehouseEvent event) {
        // Execute the lambda function
        Warehouse result = lambda.apply(event);
        context.getExtendedState().getVariables().put("warehouse", result);
        log.info("Warehouse function: " + event.getType());
        return result;
    }
}
