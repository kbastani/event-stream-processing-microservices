package demo.config;

import demo.inventory.config.InventoryEventSource;
import demo.inventory.event.InventoryEventRepository;
import demo.inventory.event.InventoryEventService;
import demo.reservation.config.ReservationEventSource;
import demo.reservation.event.ReservationEventRepository;
import demo.reservation.event.ReservationEventService;
import demo.warehouse.config.WarehouseEventSource;
import demo.warehouse.event.WarehouseEventRepository;
import demo.warehouse.event.WarehouseEventService;
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
    public InventoryEventService inventoryEventService(RestTemplate restTemplate, InventoryEventRepository
            inventoryEventRepository, InventoryEventSource eventStream) {
        return new InventoryEventService(inventoryEventRepository, eventStream, restTemplate);
    }

    @Bean
    public WarehouseEventService warehouseEventService(RestTemplate restTemplate, WarehouseEventRepository
            warehouseEventRepository, WarehouseEventSource eventStream) {
        return new WarehouseEventService(warehouseEventRepository, eventStream, restTemplate);
    }

    @Bean
    public ReservationEventService reservationEventService(RestTemplate restTemplate, ReservationEventRepository
            reservationEventRepository, ReservationEventSource eventStream) {
        return new ReservationEventService(reservationEventRepository, eventStream, restTemplate);
    }
}
