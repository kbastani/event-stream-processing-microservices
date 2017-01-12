package demo.inventory.config;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

public interface InventoryEventSource {
    String OUTPUT = "inventory";

    @Output(InventoryEventSource.OUTPUT)
    MessageChannel output();
}
