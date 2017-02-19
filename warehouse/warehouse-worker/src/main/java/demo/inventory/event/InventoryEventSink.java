package demo.inventory.event;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface InventoryEventSink {
    String INPUT = "inventory";

    @Input(InventoryEventSink.INPUT)
    SubscribableChannel input();
}
