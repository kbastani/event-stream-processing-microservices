package demo.inventory.event;

import demo.inventory.InventoryStateFactory;
import demo.inventory.domain.Inventory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Profile;

/**
 * The {@link InventoryEventProcessor} monitors for a variety of {@link InventoryEvent} domain
 * events for an {@link Inventory}.
 *
 * @author kbastani
 */
@EnableAutoConfiguration
@EnableBinding(InventoryEventSink.class)
@Profile({"cloud", "development", "docker"})
public class InventoryEventProcessor {

    private InventoryStateFactory stateFactory;

    public InventoryEventProcessor(InventoryStateFactory stateFactory) {
        this.stateFactory = stateFactory;
    }

    @StreamListener(InventoryEventSink.INPUT)
    public void streamListener(InventoryEvent inventoryEvent) {
        stateFactory.apply(inventoryEvent);
    }
}
