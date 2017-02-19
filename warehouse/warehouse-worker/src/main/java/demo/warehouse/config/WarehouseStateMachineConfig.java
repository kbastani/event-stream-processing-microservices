package demo.warehouse.config;

import demo.warehouse.domain.Warehouse;
import demo.warehouse.domain.WarehouseStatus;
import demo.warehouse.event.WarehouseEvent;
import demo.warehouse.event.WarehouseEventProcessor;
import demo.warehouse.event.WarehouseEventType;
import demo.warehouse.event.WarehouseEvents;
import demo.warehouse.function.WarehouseCreated;
import demo.warehouse.function.WarehouseFunction;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.net.URI;
import java.util.EnumSet;

/**
 * A configuration adapter for describing a {@link StateMachine} factory that maps actions to functional
 * expressions. Actions are executed during transitions between a source state and a target state.
 * <p>
 * A state machine provides a robust declarative language for describing the state of an {@link Warehouse}
 * resource given a sequence of warehouseed {@link WarehouseEvents}. When an event is received
 * in {@link WarehouseEventProcessor}, an in-memory state machine is fully replicated given the
 * {@link WarehouseEvents} attached to an {@link Warehouse} resource.
 *
 * @author kbastani
 */
@Configuration
@EnableStateMachineFactory(name = "warehouseStateMachineFactory")
public class WarehouseStateMachineConfig extends EnumStateMachineConfigurerAdapter<WarehouseStatus, WarehouseEventType> {

    final private Logger log = Logger.getLogger(WarehouseStateMachineConfig.class);

    /**
     * Configures the initial conditions of a new in-memory {@link StateMachine} for {@link Warehouse}.
     *
     * @param states is the {@link StateMachineStateConfigurer} used to describe the initial condition
     */
    @Override
    public void configure(StateMachineStateConfigurer<WarehouseStatus, WarehouseEventType> states) {
        try {
            states.withStates()
                    .initial(WarehouseStatus.WAREHOUSE_CREATED)
                    .states(EnumSet.allOf(WarehouseStatus.class));
        } catch (Exception e) {
            throw new RuntimeException("State machine configuration failed", e);
        }
    }

    /**
     * Functions are mapped to actions that are triggered during the replication of a state machine. Functions
     * should only be executed after the state machine has completed replication. This method checks the state
     * context of the machine for an {@link WarehouseEvent}, which signals that the state machine is finished
     * replication.
     * <p>
     * The {@link WarehouseFunction} argument is only applied if an {@link WarehouseEvent} is provided as a
     * message header in the {@link StateContext}.
     *
     * @param context           is the state machine context that may include an {@link WarehouseEvent}
     * @param warehouseFunction is the warehouse function to apply after the state machine has completed replication
     * @return an {@link WarehouseEvent} only if this event has not yet been processed, otherwise returns null
     */
    private WarehouseEvent applyEvent(StateContext<WarehouseStatus, WarehouseEventType> context, WarehouseFunction
            warehouseFunction) {
        WarehouseEvent event = null;
        log.info(String.format("Replicate event: %s", context.getMessage().getPayload()));

        if (context.getMessageHeader("event") != null) {
            event = context.getMessageHeaders().get("event", WarehouseEvent.class);
            log.info(String.format("State replication complete: %s", event.getType()));
            warehouseFunction.apply(event);
        }

        return event;
    }

    /**
     * Configures the {@link StateMachine} that describes how {@link WarehouseEventType} drives the state
     * of an {@link Warehouse}. Events are applied as transitions from a source {@link WarehouseStatus} to
     * a target {@link WarehouseStatus}. An {@link Action} is attached to each transition, which maps to a
     * function that is executed in the context of an {@link WarehouseEvent}.
     *
     * @param transitions is the {@link StateMachineTransitionConfigurer} used to describe state transitions
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<WarehouseStatus, WarehouseEventType> transitions) {
        try {
            // Describe state machine transitions for warehouses
            transitions.withExternal()
                    .source(WarehouseStatus.WAREHOUSE_CREATED)
                    .target(WarehouseStatus.WAREHOUSE_CREATED)
                    .event(WarehouseEventType.WAREHOUSE_CREATED)
                    .action(warehouseCreated());
        } catch (Exception e) {
            throw new RuntimeException("Could not configure state machine transitions", e);
        }
    }

    @Bean
    public Action<WarehouseStatus, WarehouseEventType> warehouseCreated() {
        return context -> applyEvent(context,
                new WarehouseCreated(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("warehouse").getHref());
                    // Get the warehouse resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("warehouse").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Warehouse.class)
                            .getBody();
                }));
    }
}

