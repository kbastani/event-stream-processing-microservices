package demo.inventory.controller;

import demo.inventory.InventoryStateFactory;
import demo.inventory.event.InventoryEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/v1/inventory")
public class InventoryEventController {

    private InventoryStateFactory eventService;

    public InventoryEventController(InventoryStateFactory eventService) {
        this.eventService = eventService;
    }

    @PostMapping(path = "/events")
    public ResponseEntity handleEvent(@RequestBody InventoryEvent event) {
        return Optional.ofNullable(eventService.apply(event))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Apply event failed"));
    }
}
