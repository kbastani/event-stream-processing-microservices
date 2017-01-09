package demo.inventory.action;

import demo.domain.Action;
import demo.inventory.domain.Inventory;
import demo.inventory.domain.InventoryService;
import demo.inventory.domain.InventoryStatus;
import demo.reservation.domain.ReservationService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.function.BiFunction;

/**
 * Reserves inventory for an {@link Inventory}.
 *
 * @author Kenny Bastani
 */
@Service
public class UpdateInventoryStatus extends Action<Inventory> {
    private final Logger log = Logger.getLogger(this.getClass());

    private final ReservationService reservationService;
    private final InventoryService inventoryService;

    public UpdateInventoryStatus(ReservationService reservationService, InventoryService inventoryService) {
        this.reservationService = reservationService;
        this.inventoryService = inventoryService;
    }

    public BiFunction<Inventory, InventoryStatus, Inventory> getFunction() {
        return (inventory, inventoryStatus) -> {

            // Save rollback status
            InventoryStatus rollbackStatus = inventory.getStatus();

            try {
                // Update status
                inventory.setStatus(inventoryStatus);
                inventory = inventoryService.update(inventory);
            } catch (Exception ex) {
                log.error("Could not update the status", ex);
                inventory.setStatus(rollbackStatus);
                inventory = inventoryService.update(inventory);
                throw ex;
            }

            return inventory;
        };
    }
}
