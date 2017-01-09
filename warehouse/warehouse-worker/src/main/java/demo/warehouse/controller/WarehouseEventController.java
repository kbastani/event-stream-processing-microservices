package demo.warehouse.controller;

import demo.warehouse.WarehouseStateFactory;
import demo.warehouse.event.WarehouseEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/v1/warehouse")
public class WarehouseEventController {

    private WarehouseStateFactory eventService;

    public WarehouseEventController(WarehouseStateFactory eventService) {
        this.eventService = eventService;
    }

    @PostMapping(path = "/events")
    public ResponseEntity handleEvent(@RequestBody WarehouseEvent event) {
        return Optional.ofNullable(eventService.apply(event))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Apply event failed"));
    }
}
