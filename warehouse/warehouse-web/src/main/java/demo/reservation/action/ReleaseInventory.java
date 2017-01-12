package demo.reservation.action;

import demo.domain.Action;
import demo.inventory.domain.Inventory;
import demo.inventory.domain.InventoryService;
import demo.inventory.domain.InventoryStatus;
import demo.inventory.event.InventoryEvent;
import demo.reservation.domain.Reservation;
import demo.reservation.domain.ReservationModule;
import demo.reservation.domain.ReservationService;
import demo.reservation.domain.ReservationStatus;
import demo.reservation.event.ReservationEvent;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.function.Function;

import static demo.inventory.event.InventoryEventType.INVENTORY_RELEASED;
import static demo.reservation.event.ReservationEventType.RESERVATION_FAILED;

/**
 * Release inventory for a {@link Reservation}.
 *
 * @author Kenny Bastani
 */
@Service
public class ReleaseInventory extends Action<Reservation> {
    private final Logger log = Logger.getLogger(this.getClass());
    private final InventoryService inventoryService;

    public ReleaseInventory(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public Function<Reservation, Reservation> getFunction() {
        return (reservation) -> {
            Assert.isTrue(reservation.getStatus() == ReservationStatus.RESERVATION_SUCCEEDED,
                    "Reservation must be in a succeeded state");
            Assert.notNull(reservation.getInventory(), "The reservation has no connected inventory");

            ReservationService reservationService = reservation.getModule(ReservationModule.class).getDefaultService();

            Inventory inventory = reservation.getInventory();

            try {
                // Remove the inventory and set the reservation to failed
                reservation.setInventory(null);
                reservation.setStatus(ReservationStatus.RESERVATION_FAILED);
                reservation = reservationService.update(reservation);

                // Trigger the reservation failed event
                reservation.sendAsyncEvent(new ReservationEvent(RESERVATION_FAILED, reservation));
            } catch (Exception ex) {
                log.error("Could not release the reservation's inventory", ex);
                if (reservation.getStatus() == ReservationStatus.RESERVATION_FAILED) {
                    // Rollback the attempt
                    reservation.setInventory(inventory);
                    reservation.setStatus(ReservationStatus.RESERVATION_SUCCEEDED);
                    reservation = reservationService.update(reservation);
                }
                throw ex;
            } finally {
                // Release the inventory
                inventory.setReservation(null);
                inventory.setStatus(InventoryStatus.RESERVATION_PENDING);
                inventory = inventoryService.update(inventory);

                // Trigger the inventory released event
                inventory.sendAsyncEvent(new InventoryEvent(INVENTORY_RELEASED, inventory));
            }

            return reservation;
        };
    }
}
