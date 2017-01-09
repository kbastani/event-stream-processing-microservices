package demo.inventory.action;

import demo.domain.Action;
import demo.inventory.domain.Inventory;
import org.springframework.stereotype.Service;

import java.util.function.BiConsumer;

/**
 * Reserves inventory for an {@link Inventory}.
 *
 * @author Kenny Bastani
 */
@Service
public class AddInventory extends Action<Inventory> {
    public BiConsumer<Inventory, Long> getConsumer() {
        return (inventory, warehouseId) -> {

        };
    }
}
