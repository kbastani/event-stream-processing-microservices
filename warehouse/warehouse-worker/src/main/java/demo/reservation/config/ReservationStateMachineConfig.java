package demo.reservation.config;

import demo.order.domain.Order;
import demo.reservation.domain.Reservation;
import demo.reservation.domain.ReservationStatus;
import demo.reservation.event.ReservationEvent;
import demo.reservation.event.ReservationEventProcessor;
import demo.reservation.event.ReservationEventType;
import demo.reservation.event.ReservationEvents;
import demo.reservation.function.*;
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
import java.util.HashMap;
import java.util.Map;

import static demo.order.domain.OrderStatus.ORDER_FAILED;
import static demo.order.domain.OrderStatus.RESERVATION_FAILED;
import static demo.order.domain.OrderStatus.RESERVATION_PENDING;

/**
 * A configuration adapter for describing a {@link StateMachine} factory that maps actions to functional
 * expressions. Actions are executed during transitions between a source state and a target state.
 * <p>
 * A state machine provides a robust declarative language for describing the state of an {@link Reservation}
 * resource given a sequence of reservationed {@link ReservationEvents}. When an event is received
 * in {@link ReservationEventProcessor}, an in-memory state machine is fully replicated given the
 * {@link ReservationEvents} attached to an {@link Reservation} resource.
 *
 * @author kbastani
 */
@Configuration
@EnableStateMachineFactory(name = "reservationStateMachineFactory")
public class ReservationStateMachineConfig extends EnumStateMachineConfigurerAdapter<ReservationStatus,
        ReservationEventType> {

    final private Logger log = Logger.getLogger(ReservationStateMachineConfig.class);

    /**
     * Configures the initial conditions of a new in-memory {@link StateMachine} for {@link Reservation}.
     *
     * @param states is the {@link StateMachineStateConfigurer} used to describe the initial condition
     */
    @Override
    public void configure(StateMachineStateConfigurer<ReservationStatus, ReservationEventType> states) {
        try {
            states.withStates()
                    .initial(ReservationStatus.RESERVATION_CREATED)
                    .states(EnumSet.allOf(ReservationStatus.class));
        } catch (Exception e) {
            throw new RuntimeException("State machine configuration failed", e);
        }
    }

    /**
     * Functions are mapped to actions that are triggered during the replication of a state machine. Functions
     * should only be executed after the state machine has completed replication. This method checks the state
     * context of the machine for an {@link ReservationEvent}, which signals that the state machine is finished
     * replication.
     * <p>
     * The {@link ReservationFunction} argument is only applied if an {@link ReservationEvent} is provided as a
     * message header in the {@link StateContext}.
     *
     * @param context             is the state machine context that may include an {@link ReservationEvent}
     * @param reservationFunction is the reservation function to apply after the state machine has completed replication
     * @return an {@link ReservationEvent} only if this event has not yet been processed, otherwise returns null
     */
    private ReservationEvent applyEvent(StateContext<ReservationStatus, ReservationEventType> context,
            ReservationFunction
                    reservationFunction) {
        ReservationEvent event = null;
        log.info(String.format("Replicate event: %s", context.getMessage().getPayload()));

        if (context.getMessageHeader("event") != null) {
            event = context.getMessageHeaders().get("event", ReservationEvent.class);
            log.info(String.format("State replication complete: %s", event.getType()));
            reservationFunction.apply(event);
        }

        return event;
    }

    /**
     * Configures the {@link StateMachine} that describes how {@link ReservationEventType} drives the state
     * of an {@link Reservation}. Events are applied as transitions from a source {@link ReservationStatus} to
     * a target {@link ReservationStatus}. An {@link Action} is attached to each transition, which maps to a
     * function that is executed in the context of an {@link ReservationEvent}.
     *
     * @param transitions is the {@link StateMachineTransitionConfigurer} used to describe state transitions
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<ReservationStatus, ReservationEventType> transitions) {
        try {
            // Describe state machine transitions for reservations
            transitions.withExternal()
                    .source(ReservationStatus.RESERVATION_CREATED)
                    .target(ReservationStatus.RESERVATION_CREATED)
                    .event(ReservationEventType.RESERVATION_CREATED)
                    .action(reservationCreated())
                    .and()
                    .withExternal()
                    .source(ReservationStatus.RESERVATION_CREATED)
                    .target(ReservationStatus.RESERVATION_PENDING)
                    .event(ReservationEventType.RESERVATION_REQUESTED)
                    .action(reservationRequested())
                    .and()
                    .withExternal()
                    .source(ReservationStatus.RESERVATION_PENDING)
                    .target(ReservationStatus.ORDER_CONNECTED)
                    .event(ReservationEventType.ORDER_CONNECTED)
                    .action(orderConnected())
                    .and()
                    .withExternal()
                    .source(ReservationStatus.ORDER_CONNECTED)
                    .target(ReservationStatus.INVENTORY_CONNECTED)
                    .event(ReservationEventType.INVENTORY_CONNECTED)
                    .action(inventoryConnected())
                    .and()
                    .withExternal()
                    .source(ReservationStatus.INVENTORY_CONNECTED)
                    .target(ReservationStatus.RESERVATION_SUCCEEDED)
                    .event(ReservationEventType.RESERVATION_SUCCEEDED)
                    .action(reservationSucceeded())
                    .and()
                    .withExternal()
                    .source(ReservationStatus.ORDER_CONNECTED)
                    .target(ReservationStatus.RESERVATION_FAILED)
                    .event(ReservationEventType.RESERVATION_FAILED)
                    .action(reservationFailed());
        } catch (Exception e) {
            throw new RuntimeException("Could not configure state machine transitions", e);
        }
    }

    @Bean
    public Action<ReservationStatus, ReservationEventType> reservationCreated() {
        return context -> applyEvent(context,
                new ReservationCreated(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("reservation").getHref());
                    // Get the reservation resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("reservation").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Reservation.class)
                            .getBody();
                }));
    }

    @Bean
    public Action<ReservationStatus, ReservationEventType> reservationRequested() {
        return context -> applyEvent(context,
                new ReservationRequested(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("reservation").getHref());
                    // Get the reservation resource for the event
                    Traverson orderResource = new Traverson(
                            URI.create(event.getLink("order").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    Traverson reservationResource = new Traverson(
                            URI.create(event.getLink("reservation").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    Reservation reservation = reservationResource.follow("self")
                            .toObject(Reservation.class);

                    Map<String, Object> template = new HashMap<>();
                    template.put("reservationId", reservation.getIdentity());

                    // Connect order to reservation
                    Order order = orderResource.follow("self", "commands", "addReservation")
                            .withTemplateParameters(template)
                            .toObject(Order.class);

                    template = new HashMap<>();
                    template.put("orderId", order.getIdentity());

                    // Connect reservation to order
                    reservation = reservationResource.follow("commands", "connectOrder")
                            .withTemplateParameters(template)
                            .toObject(Reservation.class);

                    return reservation;
                }));
    }

    @Bean
    public Action<ReservationStatus, ReservationEventType> orderConnected() {
        return context -> applyEvent(context,
                new OrderConnected(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("reservation").getHref());
                    // Get the reservation resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("reservation").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    // Connect inventory to reservation
                    return traverson.follow("self", "commands", "connectInventory")
                            .toObject(Reservation.class);
                }));
    }

    @Bean
    public Action<ReservationStatus, ReservationEventType> inventoryConnected() {
        return context -> applyEvent(context,
                new InventoryConnected(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("reservation").getHref());
                    // Get the reservation resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("reservation").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Reservation.class)
                            .getBody();
                }));
    }

    @Bean
    public Action<ReservationStatus, ReservationEventType> reservationSucceeded() {
        return context -> applyEvent(context,
                new ReservationSucceeded(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("reservation").getHref());

                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("reservation").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    Order order = traverson.follow("self", "order").toObject(Order.class);
                    Reservation reservation = null;

                    // Check order status and release inventory if it has failed
                    if (order.getStatus() == RESERVATION_FAILED || order.getStatus() == ORDER_FAILED) {
                        reservation = traverson.follow("self", "commands", "releaseInventory")
                                .toObject(Reservation.class);
                    } else if (order.getStatus() == RESERVATION_PENDING) {
                        traverson.follow("self", "order", "commands", "completeReservation")
                                .toObject(Order.class);
                    }

                    if (reservation == null)
                        reservation = traverson.follow("self")
                                .toEntity(Reservation.class)
                                .getBody();

                    return reservation;
                }));
    }

    @Bean
    public Action<ReservationStatus, ReservationEventType> reservationFailed() {
        return context -> applyEvent(context,
                new ReservationFailed(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("reservation").getHref());

                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("reservation").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    // Get the attached order
                    Order order = traverson.follow("self", "order").toObject(Order.class);

                    // Complete the reservation if the status is still pending
                    if (order.getStatus() == RESERVATION_PENDING) {
                        traverson.follow("self", "order", "commands", "completeReservation")
                                .toObject(Order.class);
                    }

                    return traverson.follow("self")
                            .toEntity(Reservation.class)
                            .getBody();
                }));
    }
}

