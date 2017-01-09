package demo.inventory.domain;

import demo.domain.Service;
import demo.inventory.repository.InventoryRepository;
import demo.reservation.domain.Reservation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@org.springframework.stereotype.Service
public class InventoryService extends Service<Inventory, Long> {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    /**
     * Create a new {@link Inventory} entity.
     *
     * @param inventory is the {@link Inventory} to create
     * @return the newly created {@link Inventory}
     */
    public Inventory create(Inventory inventory) {

        // Save the inventory to the repository
        inventory = inventoryRepository.saveAndFlush(inventory);

        return inventory;
    }

    /**
     * Get an {@link Inventory} entity for the supplied identifier.
     *
     * @param id is the unique identifier of a {@link Inventory} entity
     * @return an {@link Inventory} entity
     */
    public Inventory get(Long id) {
        return inventoryRepository.findOne(id);
    }

    /**
     * Update an {@link Inventory} entity with the supplied identifier.
     *
     * @param inventory is the {@link Inventory} containing updated fields
     * @return the updated {@link Inventory} entity
     */
    public Inventory update(Inventory inventory) {
        Assert.notNull(inventory.getIdentity(), "Inventory id must be present in the resource URL");
        Assert.notNull(inventory, "Inventory request body cannot be null");

        Assert.state(inventoryRepository.exists(inventory.getIdentity()),
                "The inventory with the supplied id does not exist");

        Inventory currentInventory = get(inventory.getIdentity());
        currentInventory.setStatus(inventory.getStatus());
        currentInventory.setProductId(inventory.getProductId());
        currentInventory.setReservation(inventory.getReservation());
        currentInventory.setWarehouse(inventory.getWarehouse());

        return inventoryRepository.saveAndFlush(currentInventory);
    }

    /**
     * Delete the {@link Inventory} with the supplied identifier.
     *
     * @param id is the unique identifier for the {@link Inventory}
     */
    public boolean delete(Long id) {
        Assert.state(inventoryRepository.exists(id),
                "The inventory with the supplied id does not exist");
        this.inventoryRepository.delete(id);
        return true;
    }

    /**
     * Find available inventory in the warehouse for a product identifier
     *
     * @param reservation is the reservation to connect to the inventory
     * @return the first available inventory in the warehouse or null
     */
    @Transactional
    public Inventory findAvailableInventory(Reservation reservation) {
        Assert.notNull(reservation.getWarehouse(), "Reservation must be connected to a warehouse");
        Assert.notNull(reservation.getProductId(), "Reservation must contain a valid product identifier");

        Inventory inventory = inventoryRepository
                .findFirstInventoryByWarehouseIdAndProductIdAndStatus(reservation.getWarehouse()
                        .getIdentity(), reservation.getProductId(), InventoryStatus.RESERVATION_PENDING)
                .orElse(null);

        if (inventory != null) {
            // Reserve the inventory
            inventory = inventory.reserve(reservation.getIdentity());
        }

        return inventory;
    }
}
