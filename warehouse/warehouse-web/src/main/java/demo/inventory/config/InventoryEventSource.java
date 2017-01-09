package demo.inventory.config;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;

public interface InventoryEventSource extends Source {
    String OUTPUT = "inventory";

    @Override
    @Output(InventoryEventSource.OUTPUT)
    MessageChannel output();
}
