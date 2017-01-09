package demo.warehouse.config;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;

public interface WarehouseEventSource extends Source {
    String OUTPUT = "warehouse";

    @Override
    @Output(WarehouseEventSource.OUTPUT)
    MessageChannel output();
}
