package demo.reservation.controller;

import demo.reservation.ReservationStateFactory;
import demo.reservation.event.ReservationEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/v1/reservation")
public class ReservationEventController {

    private ReservationStateFactory eventService;

    public ReservationEventController(ReservationStateFactory eventService) {
        this.eventService = eventService;
    }

    @PostMapping(path = "/events")
    public ResponseEntity handleEvent(@RequestBody ReservationEvent event) {
        return Optional.ofNullable(eventService.apply(event))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Apply event failed"));
    }
}
