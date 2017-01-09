package demo.inventory.function;

import demo.inventory.domain.Inventory;
import demo.inventory.domain.InventoryStatus;
import demo.inventory.event.InventoryEvent;
import demo.inventory.event.InventoryEventType;
import org.apache.log4j.Logger;
import org.springframework.statemachine.StateContext;

import java.util.function.Function;

public class ReservationConnected extends InventoryFunction {

    final private Logger log = Logger.getLogger(ReservationConnected.class);

    public ReservationConnected(StateContext<InventoryStatus, InventoryEventType> context, Function<InventoryEvent,
            Inventory> lambda) {
        super(context, lambda);
    }

    /**
     * Apply an {@link InventoryEvent} to the lambda function that was provided through the
     * constructor of this {@link InventoryFunction}.
     *
     * @param event is the {@link InventoryEvent} to apply to the lambda function
     */
    @Override
    public Inventory apply(InventoryEvent event) {
        log.info("Executing workflow for reservation connected...");
        return super.apply(event);
    }
}
