package demo.config;

import demo.event.EventSource;
import demo.inventory.config.InventoryEventSource;
import demo.inventory.event.InventoryEventRepository;
import demo.inventory.event.InventoryEventService;
import demo.reservation.config.ReservationEventSource;
import demo.reservation.event.ReservationEventRepository;
import demo.reservation.event.ReservationEventService;
import demo.warehouse.config.WarehouseEventSource;
import demo.warehouse.event.WarehouseEventRepository;
import demo.warehouse.event.WarehouseEventService;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Overrides default auto-configuration for the default event service.
 *
 * @author Kenny Bastani
 */
@Configuration
public class EventConfig {

    @Bean
    public EventSource inventoryChannel(InventoryEventSource eventSource) {
        return new EventSource(eventSource.output());
    }

    @Bean
    public EventSource warehouseChannel(WarehouseEventSource eventSource) {
        return new EventSource(eventSource.output());
    }

    @Bean
    public EventSource reservationChannel(ReservationEventSource eventSource) {
        return new EventSource(eventSource.output());
    }

    @Bean
    public InventoryEventService inventoryEventService(RestTemplate restTemplate, InventoryEventRepository
            inventoryEventRepository, InventoryEventSource eventStream, Source source) {
        return new InventoryEventService(inventoryEventRepository, inventoryChannel(eventStream), restTemplate, source);
    }

    @Bean
    public WarehouseEventService warehouseEventService(RestTemplate restTemplate, WarehouseEventRepository
            warehouseEventRepository, WarehouseEventSource eventStream, Source source) {
        return new WarehouseEventService(warehouseEventRepository, warehouseChannel(eventStream), restTemplate, source);
    }

    @Bean
    public ReservationEventService reservationEventService(RestTemplate restTemplate, ReservationEventRepository
            reservationEventRepository, ReservationEventSource eventStream, Source source) {
        return new ReservationEventService(reservationEventRepository, reservationChannel(eventStream), restTemplate,
                source);
    }


}
