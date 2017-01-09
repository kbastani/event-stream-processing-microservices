package demo.warehouse;

import demo.warehouse.domain.Warehouse;
import demo.warehouse.domain.WarehouseStatus;
import demo.warehouse.event.WarehouseEvent;
import demo.warehouse.event.WarehouseEventType;
import demo.warehouse.event.WarehouseEvents;
import org.apache.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Service
public class WarehouseStateFactory {

    final private Logger log = Logger.getLogger(WarehouseStateFactory.class);
    final private WarehouseStateService stateService;

    public WarehouseStateFactory(WarehouseStateService stateService) {
        this.stateService = stateService;
    }

    public Warehouse apply(WarehouseEvent warehouseEvent) {
        Assert.notNull(warehouseEvent, "Cannot apply a null event");
        Assert.notNull(warehouseEvent.getId(), "The event payload's identity link was not found");

        StateMachine<WarehouseStatus, WarehouseEventType> stateMachine = getStateMachine(warehouseEvent);
        stateMachine.stop();

        return stateMachine.getExtendedState().get("warehouse", Warehouse.class);
    }

    private StateMachine<WarehouseStatus, WarehouseEventType> getStateMachine(WarehouseEvent warehouseEvent) {
        Link eventId = warehouseEvent.getId();
        log.info(String.format("Warehouse event received: %s", eventId));

        StateMachine<WarehouseStatus, WarehouseEventType> stateMachine;
        Map<String, Object> contextMap;
        WarehouseEvents eventLog;

        eventLog = getEventLog(warehouseEvent);
        contextMap = getEventHeaders(warehouseEvent);
        stateMachine = stateService.newStateMachine();

        // Replicate the aggregate state
        eventLog.getContent().stream()
                .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                .forEach(e -> stateMachine.sendEvent(MessageBuilder.createMessage(e.getType(), e.getId()
                        .equals(eventId) ? new MessageHeaders(contextMap) : new MessageHeaders(null))));

        return stateMachine;
    }

    private Map<String, Object> getEventHeaders(WarehouseEvent warehouseEvent) {
        Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("event", warehouseEvent);
        return headerMap;
    }

    private WarehouseEvents getEventLog(WarehouseEvent event) {
        // Follow the hypermedia link to fetch the attached warehouse
        Traverson traverson = new Traverson(
                URI.create(event.getLink("warehouse")
                        .getHref()),
                MediaTypes.HAL_JSON
        );

        // Get the event log for the attached warehouse resource
        return traverson.follow("events")
                .toEntity(WarehouseEvents.class)
                .getBody();
    }
}
