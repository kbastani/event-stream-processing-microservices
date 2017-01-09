package demo.warehouse.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import demo.domain.AbstractEntity;
import demo.domain.Aggregate;
import demo.domain.Command;
import demo.domain.Module;
import demo.inventory.domain.Inventory;
import demo.order.domain.Order;
import demo.warehouse.action.ReserveOrder;
import demo.warehouse.controller.WarehouseController;
import demo.warehouse.event.WarehouseEvent;
import org.springframework.hateoas.Link;

import javax.persistence.*;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@Entity
public class Warehouse extends AbstractEntity<WarehouseEvent, Long> {
    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(value = EnumType.STRING)
    private WarehouseStatus status;

    @OneToOne(cascade = CascadeType.ALL)
    private Address address;

    @JsonIgnore
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "warehouse")
    private List<Inventory> inventory;

    public Warehouse() {
        this.status = WarehouseStatus.WAREHOUSE_CREATED;
    }

    @JsonProperty("warehouseId")
    @Override
    public Long getIdentity() {
        return this.id;
    }

    @Override
    public void setIdentity(Long id) {
        this.id = id;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public WarehouseStatus getStatus() {
        return status;
    }

    public void setStatus(WarehouseStatus status) {
        this.status = status;
    }

    public List<Inventory> getInventory() {
        return inventory;
    }

    public void setInventory(List<Inventory> inventory) {
        this.inventory = inventory;
    }

    @JsonIgnore
    @Command(method = "reserveOrder", controller = WarehouseController.class)
    public Warehouse reserveOrder(Order order) {
        getAction(ReserveOrder.class)
                .getConsumer()
                .accept(this, order);
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Module<A>, A extends Aggregate<WarehouseEvent, Long>> T getModule() throws
            IllegalArgumentException {
        WarehouseModule warehouseModule = getModule(WarehouseModule.class);
        return (T) warehouseModule;
    }

    /**
     * Returns the {@link Link} with a rel of {@link Link#REL_SELF}.
     */
    @Override
    public Link getId() {
        return linkTo(WarehouseController.class)
                .slash("warehouses")
                .slash(getIdentity())
                .withSelfRel();
    }
}
