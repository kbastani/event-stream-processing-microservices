package demo.warehouse.event;

import demo.warehouse.WarehouseStateFactory;
import demo.warehouse.domain.Warehouse;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Profile;

/**
 * The {@link WarehouseEventProcessor} monitors for a variety of {@link WarehouseEvent} domain
 * events for an {@link Warehouse}.
 *
 * @author kbastani
 */
@EnableAutoConfiguration
@EnableBinding(WarehouseEventSink.class)
@Profile({"cloud", "development", "docker"})
public class WarehouseEventProcessor {

    private WarehouseStateFactory stateFactory;

    public WarehouseEventProcessor(WarehouseStateFactory stateFactory) {
        this.stateFactory = stateFactory;
    }

    @StreamListener(WarehouseEventSink.INPUT)
    public void streamListener(WarehouseEvent warehouseEvent) {
        stateFactory.apply(warehouseEvent);
    }
}
