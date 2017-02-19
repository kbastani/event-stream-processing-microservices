package demo.reservation.action;

import demo.domain.Action;
import demo.inventory.domain.Inventory;
import demo.inventory.domain.InventoryService;
import demo.inventory.domain.InventoryStatus;
import demo.reservation.domain.Reservation;
import demo.reservation.domain.ReservationModule;
import demo.reservation.domain.ReservationService;
import demo.reservation.domain.ReservationStatus;
import demo.reservation.event.ReservationEvent;
import demo.reservation.exception.OutOfStockException;
import org.apache.log4j.Logger;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Random;

import static demo.reservation.event.ReservationEventType.*;

/**
 * Reserves reservation for an {@link Reservation}.
 *
 * @author Kenny Bastani
 */
@Service
public class ConnectInventory extends Action<Reservation> {
    private final Logger log = Logger.getLogger(this.getClass());
    private final DiscoveryClient discoveryClient;
    private final InventoryService inventoryService;

    public ConnectInventory(DiscoveryClient discoveryClient, InventoryService inventoryService) {
        this.discoveryClient = discoveryClient;
        this.inventoryService = inventoryService;
    }

    public Reservation apply(Reservation reservation) {
        Assert.isTrue(reservation.getStatus() == ReservationStatus.ORDER_CONNECTED,
                "Reservation must be in an order connected state");

        ReservationService reservationService = reservation.getModule(ReservationModule.class).getDefaultService();

        // Set reservation to pending
        reservation.setStatus(ReservationStatus.RESERVATION_PENDING);
        reservation = reservationService.update(reservation);

        // Get available inventory and connect reservation in an atomic transaction
        Inventory inventory = inventoryService.findAvailableInventory(reservation);

        try {
            if (inventory == null) {
                // Inventory is out of stock, fail the reservation process
                reservation.setStatus(ReservationStatus.RESERVATION_FAILED);
                reservation = reservationService.update(reservation);

                // Trigger reservation failed event
                reservation.sendAsyncEvent(new ReservationEvent(RESERVATION_FAILED, reservation));

                // Throw the out of stock exception
                throw new OutOfStockException("Inventory for reservation is unavailable in warehouse: "
                        .concat(reservation.getId().toString()));
            }

            inventory.setReservation(reservation);
            inventory.setStatus(InventoryStatus.RESERVATION_CONNECTED);
            inventory = inventoryService.update(inventory);

            // Set inventory on reservation and mark successful
            reservation.setInventory(inventory);
            reservation.setStatus(ReservationStatus.RESERVATION_SUCCEEDED);
            reservation = reservationService.update(reservation);


            // Trigger the inventory connected event
            reservation.sendAsyncEvent(new ReservationEvent(INVENTORY_CONNECTED, reservation),
                    reservation.getInventory().getId().withRel("inventory"));
        } catch (Exception ex) {
            log.error("Could not connect reservation to order", ex);
            if (reservation.getStatus() != ReservationStatus.RESERVATION_FAILED) {
                // Rollback the reservation attempt
                if (inventory != null) {
                    inventory.setReservation(null);
                    inventory.setStatus(InventoryStatus.RESERVATION_PENDING);
                    inventoryService.update(inventory);
                }

                reservation.setInventory(null);
                reservation.setStatus(ReservationStatus.ORDER_CONNECTED);
                reservation = reservationService.update(reservation);
            }

            throw ex;
        } finally {
            if (reservation.getStatus() == ReservationStatus.RESERVATION_SUCCEEDED) {
                Link inventoryLink = reservation.getInventory().getId().withRel("inventory");
                Link orderLink = getRemoteLink("order-web", "/v1/orders/{id}", reservation.getOrderId(), "order");
                reservation
                        .sendAsyncEvent(new ReservationEvent(RESERVATION_SUCCEEDED, reservation), inventoryLink,
                                orderLink);
            }
        }

        return reservation;
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
