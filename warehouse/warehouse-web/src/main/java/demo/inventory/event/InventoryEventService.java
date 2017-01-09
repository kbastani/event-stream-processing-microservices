package demo.inventory.event;

import demo.event.BasicEventService;
import demo.inventory.config.InventoryEventSource;
import org.springframework.web.client.RestTemplate;

public class InventoryEventService extends BasicEventService<InventoryEvent, Long> {
    public InventoryEventService(InventoryEventRepository eventRepository, InventoryEventSource eventStream,
            RestTemplate restTemplate) {
        super(eventRepository, eventStream, restTemplate);
    }
}
