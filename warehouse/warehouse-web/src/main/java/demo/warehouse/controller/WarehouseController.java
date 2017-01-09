package demo.warehouse.controller;

import demo.event.Events;
import demo.inventory.controller.InventoryController;
import demo.inventory.domain.Inventory;
import demo.inventory.domain.InventoryItems;
import demo.inventory.domain.InventoryService;
import demo.inventory.domain.InventoryStatus;
import demo.inventory.event.InventoryEvent;
import demo.inventory.event.InventoryEventType;
import demo.order.domain.Order;
import demo.warehouse.domain.Warehouse;
import demo.warehouse.domain.WarehouseService;
import demo.warehouse.event.WarehouseEvent;
import demo.warehouse.event.WarehouseEventService;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final InventoryService inventoryService;
    private final WarehouseEventService eventService;
    private final DiscoveryClient discoveryClient;

    public WarehouseController(WarehouseService warehouseService, InventoryService inventoryService,
            WarehouseEventService eventService, DiscoveryClient discoveryClient) {
        this.warehouseService = warehouseService;
        this.inventoryService = inventoryService;
        this.eventService = eventService;
        this.discoveryClient = discoveryClient;
    }

    @PostMapping(path = "/warehouses")
    public ResponseEntity createWarehouse(@RequestBody Warehouse warehouse) {
        return Optional.ofNullable(createWarehouseResource(warehouse))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Warehouse creation failed"));
    }

    @PutMapping(path = "/warehouses/{id}")
    public ResponseEntity updateWarehouse(@RequestBody Warehouse warehouse, @PathVariable Long id) {
        return Optional.ofNullable(updateWarehouseResource(id, warehouse))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Warehouse update failed"));
    }

    @RequestMapping(path = "/warehouses/{id}")
    public ResponseEntity getWarehouse(@PathVariable Long id) {
        return Optional.ofNullable(getWarehouseResource(warehouseService.get(id)))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(path = "/warehouses/{id}")
    public ResponseEntity deleteWarehouse(@PathVariable Long id) {
        return Optional.ofNullable(warehouseService.delete(id))
                .map(e -> new ResponseEntity<>(HttpStatus.NO_CONTENT))
                .orElseThrow(() -> new RuntimeException("Warehouse deletion failed"));
    }

    @RequestMapping(path = "/warehouses/{id}/events")
    public ResponseEntity getWarehouseEvents(@PathVariable Long id) {
        return Optional.of(getWarehouseEventResources(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Could not get warehouse events"));
    }

    @RequestMapping(path = "/warehouses/{id}/events/{eventId}")
    public ResponseEntity getWarehouseEvent(@PathVariable Long id, @PathVariable Long eventId) {
        return Optional.of(getEventResource(eventId))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Could not get order events"));
    }

    @PostMapping(path = "/warehouses/{id}/events")
    public ResponseEntity appendWarehouseEvents(@PathVariable Long id, @RequestBody WarehouseEvent event) {
        return Optional.ofNullable(appendEventResource(id, event))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Append warehouse event failed"));
    }

    @RequestMapping(path = "/warehouses/{id}/inventory")
    public ResponseEntity getWarehouseInventory(@PathVariable Long id) {
        return Optional.of(getWarehouseInventoryResources(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Could not get warehouse inventory"));
    }

    @RequestMapping(path = "/warehouses/{id}/inventory", method = RequestMethod.POST)
    public ResponseEntity addWarehouseInventory(@PathVariable Long id, @RequestBody Inventory inventory) {
        return Optional.of(addInventoryResource(id, inventory))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Could not add inventory"));
    }

    @RequestMapping(path = "/warehouses/{id}/commands")
    public ResponseEntity getCommands(@PathVariable Long id) {
        return Optional.ofNullable(getCommandsResources(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The warehouse could not be found"));
    }

    @RequestMapping(path = "/warehouses/{id}/commands/reserveOrder", method = RequestMethod.POST)
    public ResponseEntity reserveOrder(@PathVariable Long id, @RequestBody Order order) {
        return Optional.ofNullable(warehouseService.get(id).reserveOrder(order))
                .map(e -> new ResponseEntity<>(getWarehouseResource(e), HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @RequestMapping(path = "/warehouses/search/findWarehouseWithInventory", method = RequestMethod.POST)
    public ResponseEntity findWarehouseWithInventory(@RequestBody Order order) {
        return Optional.ofNullable(warehouseService.findWarehouseForOrder(order))
                .map(e -> new ResponseEntity<>(getWarehouseResource(e), HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Creates a new {@link Warehouse} entity and persists the result to the repository.
     *
     * @param warehouse is the {@link Warehouse} model used to create a new warehouse
     * @return a hypermedia resource for the newly created {@link Warehouse}
     */
    private Resource<Warehouse> createWarehouseResource(Warehouse warehouse) {
        Assert.notNull(warehouse, "Warehouse body must not be null");

        // Create the new warehouse
        warehouse = warehouseService.create(warehouse);

        return getWarehouseResource(warehouse);
    }

    /**
     * Update a {@link Warehouse} entity for the provided identifier.
     *
     * @param id        is the unique identifier for the {@link Warehouse} update
     * @param warehouse is the entity representation containing any updated {@link Warehouse} fields
     * @return a hypermedia resource for the updated {@link Warehouse}
     */
    private Resource<Warehouse> updateWarehouseResource(Long id, Warehouse warehouse) {
        warehouse.setIdentity(id);
        return getWarehouseResource(warehouseService.update(warehouse));
    }

    /**
     * Appends an {@link WarehouseEvent} domain event to the event log of the {@link Warehouse} aggregate with the
     * specified warehouseId.
     *
     * @param warehouseId is the unique identifier for the {@link Warehouse}
     * @param event       is the {@link WarehouseEvent} that attempts to alter the state of the {@link Warehouse}
     * @return a hypermedia resource for the newly appended {@link WarehouseEvent}
     */
    private Resource<WarehouseEvent> appendEventResource(Long warehouseId, WarehouseEvent event) {
        Resource<WarehouseEvent> eventResource = null;

        warehouseService.get(warehouseId)
                .sendAsyncEvent(event);

        if (event != null) {
            eventResource = new Resource<>(event,
                    linkTo(WarehouseController.class)
                            .slash("warehouses")
                            .slash(warehouseId)
                            .slash("events")
                            .slash(event.getEventId())
                            .withSelfRel(),
                    linkTo(WarehouseController.class)
                            .slash("warehouses")
                            .slash(warehouseId)
                            .withRel("warehouse")
            );
        }

        return eventResource;
    }

    private Resource<Inventory> addInventoryResource(Long warehouseId, Inventory inventory) {
        Resource<Inventory> inventoryResource;

        Warehouse warehouse = warehouseService.get(warehouseId);

        Assert.notNull(warehouse, "Warehouse for the identifier could not be found");

        inventory.setWarehouse(warehouse);
        inventory.setStatus(InventoryStatus.INVENTORY_CREATED);
        inventory = inventoryService.create(inventory);
        inventory.sendAsyncEvent(new InventoryEvent(InventoryEventType.INVENTORY_CREATED, inventory));

        inventoryResource = new Resource<>(inventory,
                linkTo(InventoryController.class)
                        .slash("inventory")
                        .slash(inventory.getIdentity())
                        .withSelfRel(),
                linkTo(WarehouseController.class)
                        .slash("warehouses")
                        .slash(warehouseId)
                        .withRel("warehouse")
        );

        return inventoryResource;
    }

    private WarehouseEvent getEventResource(Long eventId) {
        return eventService.findOne(eventId);
    }

    private Events getWarehouseEventResources(Long id) {
        return eventService.find(id);
    }

    private InventoryItems getWarehouseInventoryResources(Long id) {
        Warehouse warehouse = warehouseService.get(id);
        Assert.notNull(warehouse, "Warehouse could not be found");

        InventoryItems warehouseInventory = new InventoryItems(warehouse.getInventory());

        warehouseInventory.add(
                linkTo(WarehouseController.class)
                        .slash("warehouses")
                        .slash(id)
                        .slash("inventory")
                        .withSelfRel(),
                linkTo(WarehouseController.class)
                        .slash("warehouses")
                        .slash(id)
                        .withRel("warehouse")
        );

        return warehouseInventory;
    }

    private LinkBuilder linkBuilder(String name, Long id) {
        Method method;

        try {
            method = WarehouseController.class.getMethod(name, Long.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        return linkTo(WarehouseController.class, method, id);
    }

    /**
     * Get a hypermedia enriched {@link Warehouse} entity.
     *
     * @param warehouse is the {@link Warehouse} to enrich with hypermedia links
     * @return is a hypermedia enriched resource for the supplied {@link Warehouse} entity
     */
    private Resource<Warehouse> getWarehouseResource(Warehouse warehouse) {
        if (warehouse == null) return null;

        if (!warehouse.hasLink("commands")) {
            // Add command link
            warehouse.add(linkBuilder("getCommands", warehouse.getIdentity()).withRel("commands"));
        }

        if (!warehouse.hasLink("events")) {
            // Add get events link
            warehouse.add(linkBuilder("getWarehouseEvents", warehouse.getIdentity()).withRel("events"));
        }

        if (!warehouse.hasLink("inventory")) {
            // Add get inventory link
            warehouse.add(linkBuilder("getWarehouseInventory", warehouse.getIdentity()).withRel("inventory"));
        }

        return new Resource<>(warehouse);
    }

    private ResourceSupport getCommandsResources(Long id) {
        Warehouse warehouse = new Warehouse();
        warehouse.setIdentity(id);
        return new Resource<>(warehouse.getCommands());
    }
}
