package demo.warehouse.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import demo.domain.Aggregate;
import demo.domain.Module;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.UriTemplate;

import java.util.ArrayList;
import java.util.List;

public class Warehouse extends Aggregate<WarehouseEvent, Long> {

    private Long id;
    private List<WarehouseEvent> events = new ArrayList<>();
    private Address address;
    private WarehouseStatus status;

    public Warehouse() {
    }

    public Warehouse(Address address) {
        this.address = address;
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

    @JsonProperty("warehouseId")
    @Override
    public Long getIdentity() {
        return id;
    }

    public void setIdentity(Long id) {
        this.id = id;
    }

    @Override
    public List<WarehouseEvent> getEvents() {
        return events;
    }

    /**
     * Returns the {@link Link} with a rel of {@link Link#REL_SELF}.
     */
    @Override
    public Link getId() {
        return new Link(new UriTemplate("http://warehouse-web/v1/warehouses/{id}")
                .with("id", TemplateVariable.VariableType.PATH_VARIABLE)
                .expand(getIdentity())
                .toString()).withSelfRel();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Module<A>, A extends Aggregate<WarehouseEvent, Long>> T getModule() throws
            IllegalArgumentException {
        WarehouseModule warehouseModule = getModule(WarehouseModule.class);
        return (T) warehouseModule;
    }

    @Override
    public String toString() {
        return "Warehouse{" +
                "id=" + id +
                ", events=" + events +
                ", address=" + address +
                ", status=" + status +
                "} " + super.toString();
    }
}
