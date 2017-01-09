package demo.inventory.function;

import demo.inventory.domain.Inventory;
import demo.inventory.domain.InventoryStatus;
import demo.inventory.event.InventoryEvent;
import demo.inventory.event.InventoryEventType;
import org.apache.log4j.Logger;
import org.springframework.statemachine.StateContext;

import java.util.function.Function;

/**
 * The {@link InventoryFunction} is an abstraction used to map actions that are triggered by
 * state transitions on a {@link Inventory} resource on to a function. Mapped functions
 * can take multiple forms and reside either remotely or locally on the classpath of this application.
 *
 * @author kbastani
 */
public abstract class InventoryFunction {

    final private Logger log = Logger.getLogger(InventoryFunction.class);
    final protected StateContext<InventoryStatus, InventoryEventType> context;
    final protected Function<InventoryEvent, Inventory> lambda;

    /**
     * Create a new instance of a class that extends {@link InventoryFunction}, supplying
     * a state context and a lambda function used to apply {@link InventoryEvent} to a provided
     * action.
     *
     * @param context is the {@link StateContext} for a replicated state machine
     * @param lambda  is the lambda function describing an action that consumes an {@link InventoryEvent}
     */
    public InventoryFunction(StateContext<InventoryStatus, InventoryEventType> context,
                         Function<InventoryEvent, Inventory> lambda) {
        this.context = context;
        this.lambda = lambda;
    }

    /**
     * Apply an {@link InventoryEvent} to the lambda function that was provided through the
     * constructor of this {@link InventoryFunction}.
     *
     * @param event is the {@link InventoryEvent} to apply to the lambda function
     */
    public Inventory apply(InventoryEvent event) {
        // Execute the lambda function
        Inventory result = lambda.apply(event);
        context.getExtendedState().getVariables().put("inventory", result);
        log.info("Inventory function: " + event.getType());
        return result;
    }
}
