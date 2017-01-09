package demo.warehouse.event;

import demo.event.BasicEventService;
import demo.warehouse.config.WarehouseEventSource;
import org.springframework.web.client.RestTemplate;

public class WarehouseEventService extends BasicEventService<WarehouseEvent, Long> {
    public WarehouseEventService(WarehouseEventRepository eventRepository, WarehouseEventSource eventStream,
            RestTemplate restTemplate) {
        super(eventRepository, eventStream, restTemplate);
    }
}
