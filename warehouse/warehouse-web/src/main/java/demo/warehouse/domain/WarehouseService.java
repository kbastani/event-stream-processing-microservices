package demo.warehouse.domain;

import demo.domain.Service;
import demo.order.domain.LineItem;
import demo.order.domain.Order;
import demo.warehouse.repository.WarehouseRepository;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class WarehouseService extends Service<Warehouse, Long> {

    private final WarehouseRepository warehouseRepository;

    public WarehouseService(WarehouseRepository warehouseRepository) {
        this.warehouseRepository = warehouseRepository;
    }

    /**
     * Create a new {@link Warehouse} entity.
     *
     * @param warehouse is the {@link Warehouse} to create
     * @return the newly created {@link Warehouse}
     */
    public Warehouse create(Warehouse warehouse) {
        // Save the warehouse to the repository
        warehouse = warehouseRepository.saveAndFlush(warehouse);

        return warehouse;
    }

    /**
     * Get an {@link Warehouse} entity for the supplied identifier.
     *
     * @param id is the unique identifier of a {@link Warehouse} entity
     * @return an {@link Warehouse} entity
     */
    public Warehouse get(Long id) {
        return warehouseRepository.findOne(id);
    }

    /**
     * Update an {@link Warehouse} entity with the supplied identifier.
     *
     * @param warehouse is the {@link Warehouse} containing updated fields
     * @return the updated {@link Warehouse} entity
     */
    public Warehouse update(Warehouse warehouse) {
        Assert.notNull(warehouse.getIdentity(), "Warehouse id must be present in the resource URL");
        Assert.notNull(warehouse, "Warehouse request body cannot be null");

        Assert.state(warehouseRepository.exists(warehouse.getIdentity()),
                "The warehouse with the supplied id does not exist");

        Warehouse currentWarehouse = get(warehouse.getIdentity());
        currentWarehouse.setAddress(warehouse.getAddress());
        currentWarehouse.setStatus(warehouse.getStatus());

        return warehouseRepository.saveAndFlush(currentWarehouse);
    }

    /**
     * Delete the {@link Warehouse} with the supplied identifier.
     *
     * @param id is the unique identifier for the {@link Warehouse}
     */
    public boolean delete(Long id) {
        Assert.state(warehouseRepository.exists(id),
                "The warehouse with the supplied id does not exist");
        this.warehouseRepository.delete(id);
        return true;
    }

    /**
     * Finds the first warehouse that stocks all line items of an {@link Order}
     *
     * @param order is the {@link Order} containing the line items to locate
     * @return a {@link Warehouse} that has all line items in stock, or null if one could not be found
     */
    public Warehouse findWarehouseForOrder(Order order) {
        List<String> productIds = order.getLineItems().stream()
                .map(LineItem::getProductId)
                .distinct()
                .collect(Collectors.toList());

        List<Warehouse> warehouses = warehouseRepository.findAllWithInventory((long) productIds.size(), productIds);

        return warehouses.stream().findFirst().orElse(null);
    }
}
