package demo.reservation.controller;

import demo.event.EventService;
import demo.event.Events;
import demo.inventory.controller.InventoryController;
import demo.reservation.domain.Reservation;
import demo.reservation.domain.ReservationService;
import demo.reservation.event.ReservationEvent;
import demo.warehouse.controller.WarehouseController;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.hateoas.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1")
public class ReservationController {

    private final ReservationService reservationService;
    private final EventService<ReservationEvent, Long> eventService;
    private final DiscoveryClient discoveryClient;

    public ReservationController(ReservationService reservationService, EventService<ReservationEvent, Long>
            eventService, DiscoveryClient
            discoveryClient) {
        this.reservationService = reservationService;
        this.eventService = eventService;
        this.discoveryClient = discoveryClient;
    }

    @PostMapping(path = "/reservations")
    public ResponseEntity createReservation(@RequestBody Reservation reservation) {
        return Optional.ofNullable(createReservationResource(reservation))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Reservation creation failed"));
    }

    @PutMapping(path = "/reservations/{id}")
    public ResponseEntity updateReservation(@RequestBody Reservation reservation, @PathVariable Long id) {
        return Optional.ofNullable(updateReservationResource(id, reservation))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Reservation update failed"));
    }

    @RequestMapping(path = "/reservations/{id}")
    public ResponseEntity getReservation(@PathVariable Long id) {
        return Optional.ofNullable(getReservationResource(reservationService.get(id)))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(path = "/reservations/{id}")
    public ResponseEntity deleteReservation(@PathVariable Long id) {
        return Optional.ofNullable(reservationService.delete(id))
                .map(e -> new ResponseEntity<>(HttpStatus.NO_CONTENT))
                .orElseThrow(() -> new RuntimeException("Reservation deletion failed"));
    }

    @RequestMapping(path = "/reservations/{id}/events")
    public ResponseEntity getReservationEvents(@PathVariable Long id) {
        return Optional.of(getReservationEventResources(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Could not get reservation events"));
    }

    @RequestMapping(path = "/reservations/{id}/events/{eventId}")
    public ResponseEntity getReservationEvent(@PathVariable Long id, @PathVariable Long eventId) {
        return Optional.of(getEventResource(eventId))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Could not get order events"));
    }

    @PostMapping(path = "/reservations/{id}/events")
    public ResponseEntity appendReservationEvent(@PathVariable Long id, @RequestBody ReservationEvent event) {
        return Optional.ofNullable(appendEventResource(id, event))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Append reservation event failed"));
    }

    @RequestMapping(path = "/reservations/{id}/commands")
    public ResponseEntity getCommands(@PathVariable Long id) {
        return Optional.ofNullable(getCommandsResources(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The reservation could not be found"));
    }

    @RequestMapping(path = "/reservations/{id}/commands/connectInventory")
    public ResponseEntity connectInventory(@PathVariable Long id) {
        return Optional.ofNullable(reservationService.get(id))
                .map(e -> new ResponseEntity<>(getReservationResource(e.connectInventory()), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @RequestMapping(path = "/reservations/{id}/commands/releaseInventory")
    public ResponseEntity releaseInventory(@PathVariable Long id) {
        return Optional.ofNullable(reservationService.get(id))
                .map(e -> new ResponseEntity<>(getReservationResource(e.releaseInventory()), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @RequestMapping(path = "/reservations/{id}/commands/connectOrder")
    public ResponseEntity connectOrder(@PathVariable Long id, @RequestParam(value = "orderId") Long orderId) {
        return Optional.ofNullable(reservationService.get(id)
                .connectOrder(orderId))
                .map(e -> new ResponseEntity<>(getReservationResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @RequestMapping(path = "/reservations/search/findReservationsByOrderId")
    public ResponseEntity findReservationsByOrderId(@RequestParam("orderId") Long orderId) {
        return Optional.ofNullable(reservationService.findReservationsByOrderId(orderId))
                .map(e -> new ResponseEntity<>(new Resources<Reservation>(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    /**
     * Creates a new {@link Reservation} entity and persists the result to the repository.
     *
     * @param reservation is the {@link Reservation} model used to create a new reservation
     * @return a hypermedia resource for the newly created {@link Reservation}
     */
    private Resource<Reservation> createReservationResource(Reservation reservation) {
        Assert.notNull(reservation, "Reservation body must not be null");

        // Create the new reservation
        reservation = reservationService.create(reservation);

        return getReservationResource(reservation);
    }

    /**
     * Update a {@link Reservation} entity for the provided identifier.
     *
     * @param id          is the unique identifier for the {@link Reservation} update
     * @param reservation is the entity representation containing any updated {@link Reservation} fields
     * @return a hypermedia resource for the updated {@link Reservation}
     */
    private Resource<Reservation> updateReservationResource(Long id, Reservation reservation) {
        reservation.setIdentity(id);
        return getReservationResource(reservationService.update(reservation));
    }

    /**
     * Appends an {@link ReservationEvent} domain event to the event log of the {@link Reservation} aggregate with the
     * specified reservationId.
     *
     * @param reservationId is the unique identifier for the {@link Reservation}
     * @param event         is the {@link ReservationEvent} that attempts to alter the state of the {@link Reservation}
     * @return a hypermedia resource for the newly appended {@link ReservationEvent}
     */
    private Resource<ReservationEvent> appendEventResource(Long reservationId, ReservationEvent event) {
        Resource<ReservationEvent> eventResource = null;

        reservationService.get(reservationId)
                .sendAsyncEvent(event);

        if (event != null) {
            eventResource = new Resource<>(event,
                    linkTo(ReservationController.class)
                            .slash("reservations")
                            .slash(reservationId)
                            .slash("events")
                            .slash(event.getEventId())
                            .withSelfRel(),
                    linkTo(ReservationController.class)
                            .slash("reservations")
                            .slash(reservationId)
                            .withRel("reservations")
            );
        }

        return eventResource;
    }

    private ReservationEvent getEventResource(Long eventId) {
        return eventService.findOne(eventId);
    }

    private Events getReservationEventResources(Long id) {
        return eventService.find(id);
    }

    private LinkBuilder linkBuilder(String name, Long id) {
        Method method;

        try {
            method = ReservationController.class.getMethod(name, Long.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return linkTo(ReservationController.class, method, id);
    }

    /**
     * Get a hypermedia enriched {@link Reservation} entity.
     *
     * @param reservation is the {@link Reservation} to enrich with hypermedia links
     * @return is a hypermedia enriched resource for the supplied {@link Reservation} entity
     */
    private Resource<Reservation> getReservationResource(Reservation reservation) {
        if (reservation == null) return null;

        if (!reservation.hasLink("commands")) {
            // Add command link
            reservation.add(linkBuilder("getCommands", reservation.getIdentity()).withRel("commands"));
        }

        if (!reservation.hasLink("events")) {
            // Add get events link
            reservation.add(linkBuilder("getReservationEvents", reservation.getIdentity()).withRel("events"));
        }

        // Add remote order link
        if (reservation.getOrderId() != null && !reservation.hasLink("order")) {
            Link result = getRemoteLink("order-web", "/v1/orders/{id}", reservation.getOrderId(), "order");
            if (result != null)
                reservation.add(result);
        }

        if (reservation.getInventory() != null && !reservation.hasLink("inventory")) {
            // Add get inventory link
            reservation.add(linkTo(InventoryController.class)
                    .slash("inventory")
                    .slash(reservation.getInventory().getIdentity())
                    .withRel("inventory"));
        }

        if (reservation.getWarehouse() != null && !reservation.hasLink("warehouse")) {
            // Add get warehouse link
            reservation.add(linkTo(WarehouseController.class)
                    .slash("warehouses")
                    .slash(reservation.getWarehouse().getIdentity())
                    .withRel("warehouse"));
        }

        return new Resource<>(reservation);
    }

    private ResourceSupport getCommandsResources(Long id) {
        Reservation reservation = new Reservation();
        reservation.setIdentity(id);
        return new Resource<>(reservation.getCommands());
    }

    private Link getRemoteLink(String service, String relative, Object identifier, String rel) {
        Link result = null;
        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(service);
        if (serviceInstances.size() > 0) {
            ServiceInstance serviceInstance = serviceInstances.get(new Random().nextInt(serviceInstances.size()));
            result = new Link(new UriTemplate(serviceInstance.getUri()
                    .toString()
                    .concat(relative)).with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                    .expand(identifier)
                    .toString())
                    .withRel(rel);
        }
        return result;
    }
}
