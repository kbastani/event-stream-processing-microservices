package demo.warehouse.function;

import demo.warehouse.domain.*;
import demo.warehouse.event.*;
import org.apache.log4j.Logger;
import org.springframework.statemachine.StateContext;

import java.util.function.Function;

public class WarehouseCreated extends WarehouseFunction {

    final private Logger log = Logger.getLogger(WarehouseCreated.class);

    public WarehouseCreated(StateContext<WarehouseStatus, WarehouseEventType> context, Function<WarehouseEvent,
            Warehouse> lambda) {
        super(context, lambda);
    }

    /**
     * Apply an {@link WarehouseEvent} to the lambda function that was provided through the
     * constructor of this {@link WarehouseFunction}.
     *
     * @param event is the {@link WarehouseEvent} to apply to the lambda function
     */
    @Override
    public Warehouse apply(WarehouseEvent event) {
        log.info("Executing workflow for warehouse created...");
        return super.apply(event);
    }
}
