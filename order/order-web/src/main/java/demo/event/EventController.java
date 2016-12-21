package demo.event;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/v1")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping(path = "/events/{id}")
    public ResponseEntity createEvent(@RequestBody OrderEvent event, @PathVariable Long id) {
        return Optional.ofNullable(eventService.createEvent(id, event, ConsistencyModel.ACID))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new IllegalArgumentException("Event creation failed"));
    }

    @PutMapping(path = "/events/{id}")
    public ResponseEntity updateEvent(@RequestBody OrderEvent event, @PathVariable Long id) {
        return Optional.ofNullable(eventService.updateEvent(id, event))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new IllegalArgumentException("Event update failed"));
    }

    @GetMapping(path = "/events/{id}")
    public ResponseEntity getEvent(@PathVariable Long id) {
        return Optional.ofNullable(eventService.getEvent(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
