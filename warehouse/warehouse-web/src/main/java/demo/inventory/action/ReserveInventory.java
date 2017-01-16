package demo.inventory.action;

import demo.domain.Action;
import demo.inventory.domain.Inventory;
import demo.inventory.domain.InventoryService;
import demo.inventory.domain.InventoryStatus;
import demo.inventory.event.InventoryEvent;
import demo.inventory.event.InventoryEventType;
import demo.reservation.domain.Reservation;
import demo.reservation.domain.ReservationService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import static demo.inventory.domain.InventoryStatus.RESERVATION_CONNECTED;

/**
 * Reserves inventory for an {@link Inventory}.
 *
 * @author Kenny Bastani
 */
@Service
public class ReserveInventory extends Action<Inventory> {
    private final Logger log = Logger.getLogger(this.getClass());

    private final ReservationService reservationService;
    private final InventoryService inventoryService;

    public ReserveInventory(ReservationService reservationService, InventoryService inventoryService) {
        this.reservationService = reservationService;
        this.inventoryService = inventoryService;
    }

    public Inventory apply(Inventory inventory, Long reservationId) {
        Assert.isTrue(inventory.getStatus() == InventoryStatus.RESERVATION_CONNECTED,
                "Inventory must be in a reservation connected state");
        Assert.isTrue(inventory.getReservation() == null,
                "There is already a reservation attached to the inventory");

        Reservation reservation = reservationService.get(reservationId);
        Assert.notNull(reservation, "Reserve inventory failed, the reservation does not exist");

        try {
            // Trigger the reservation connected event
            inventory.sendAsyncEvent(new InventoryEvent(InventoryEventType.RESERVATION_CONNECTED, inventory));
        } catch (Exception ex) {
            log.error("Could not connect reservation to inventory", ex);
            inventory.setReservation(null);
            inventory.setStatus(InventoryStatus.RESERVATION_PENDING);
            inventory = inventoryService.update(inventory);
        } finally {
            if (inventory.getStatus() == RESERVATION_CONNECTED && inventory.getReservation() != null) {
                inventory.setStatus(InventoryStatus.INVENTORY_RESERVED);
                inventory = inventoryService.update(inventory);
                inventory.sendAsyncEvent(new InventoryEvent(InventoryEventType.INVENTORY_RESERVED, inventory));
            }
        }

        return inventory;
    }
}
