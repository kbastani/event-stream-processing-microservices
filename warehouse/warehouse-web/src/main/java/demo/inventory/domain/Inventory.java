package demo.inventory.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import demo.domain.AbstractEntity;
import demo.domain.Aggregate;
import demo.domain.Command;
import demo.domain.Module;
import demo.inventory.action.ReserveInventory;
import demo.inventory.action.UpdateInventoryStatus;
import demo.inventory.controller.InventoryController;
import demo.inventory.event.InventoryEvent;
import demo.reservation.domain.Reservation;
import demo.warehouse.domain.Warehouse;
import org.springframework.hateoas.Link;

import javax.persistence.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Entity
public class Inventory extends AbstractEntity<InventoryEvent, Long> {
    @Id
    @GeneratedValue
    private Long id;

    private String productId;

    @Enumerated(value = EnumType.STRING)
    private InventoryStatus status;

    @JsonIgnore
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Reservation reservation;

    @JsonIgnore
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Warehouse warehouse;

    public Inventory() {
        this.status = InventoryStatus.INVENTORY_CREATED;
    }

    @JsonProperty("inventoryId")
    @Override
    public Long getIdentity() {
        return this.id;
    }

    @Override
    public void setIdentity(Long id) {
        this.id = id;
    }

    public InventoryStatus getStatus() {
        return status;
    }

    public void setStatus(InventoryStatus status) {
        this.status = status;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public void setWarehouse(Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    @Command(method = "reserve", controller = InventoryController.class)
    public Inventory reserve(Long reservationId) {
        return getAction(ReserveInventory.class)
                .getFunction()
                .apply(this, reservationId);
    }

    @Command(method = "updateInventoryStatus", controller = InventoryController.class)
    public Inventory updateStatus(InventoryStatus status) {
        return getAction(UpdateInventoryStatus.class)
                .getFunction()
                .apply(this, status);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Module<A>, A extends Aggregate<InventoryEvent, Long>> T getModule() throws
            IllegalArgumentException {
        InventoryModule inventoryModule = getModule(InventoryModule.class);
        return (T) inventoryModule;
    }

    /**
     * Returns the {@link Link} with a rel of {@link Link#REL_SELF}.
     */
    @Override
    public Link getId() {
        return linkTo(InventoryController.class)
                .slash("inventory")
                .slash(getIdentity())
                .withSelfRel();
    }
}
