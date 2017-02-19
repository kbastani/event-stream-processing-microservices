package demo.order.controller;

import demo.order.StateFactory;
import demo.order.event.OrderEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/v1")
public class EventController {

    private StateFactory eventService;

    public EventController(StateFactory eventService) {
        this.eventService = eventService;
    }

    @PostMapping(path = "/events")
    public ResponseEntity handleEvent(@RequestBody OrderEvent event) {
        return Optional.ofNullable(eventService.apply(event))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Apply event failed"));
    }
}
