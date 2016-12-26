package demo.config;

import demo.event.OrderEvent;
import demo.event.OrderEventType;
import demo.function.*;
import demo.order.Order;
import demo.order.OrderStatus;
import demo.payment.Payment;
import demo.stream.OrderStream;
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

/**
 * A configuration adapter for describing a {@link StateMachine} factory that maps actions to functional
 * expressions. Actions are executed during transitions between a source state and a target state.
 * <p>
 * A state machine provides a robust declarative language for describing the state of an {@link Order}
 * resource given a sequence of ordered {@link demo.event.OrderEvents}. When an event is received
 * in {@link OrderStream}, an in-memory state machine is fully replicated given the
 * {@link demo.event.OrderEvents} attached to an {@link Order} resource.
 *
 * @author kbastani
 */
@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<OrderStatus, OrderEventType> {

    final private Logger log = Logger.getLogger(StateMachineConfig.class);

    /**
     * Configures the initial conditions of a new in-memory {@link StateMachine} for {@link Order}.
     *
     * @param states is the {@link StateMachineStateConfigurer} used to describe the initial condition
     */
    @Override
    public void configure(StateMachineStateConfigurer<OrderStatus, OrderEventType> states) {
        try {
            // Describe the initial condition of the order state machine
            states.withStates()
                    .initial(OrderStatus.ORDER_CREATED)
                    .states(EnumSet.allOf(OrderStatus.class));
        } catch (Exception e) {
            throw new RuntimeException("State machine configuration failed", e);
        }
    }

    /**
     * Configures the {@link StateMachine} that describes how {@link OrderEventType} drives the state
     * of an {@link Order}. Events are applied as transitions from a source {@link OrderStatus} to
     * a target {@link OrderStatus}. An {@link Action} is attached to each transition, which maps to a
     * function that is executed in the context of an {@link OrderEvent}.
     *
     * @param transitions is the {@link StateMachineTransitionConfigurer} used to describe state transitions
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<OrderStatus, OrderEventType> transitions) {
        try {
            // Describe state machine transitions for orders
            transitions.withExternal()
                    .source(OrderStatus.ORDER_CREATED)
                    .target(OrderStatus.ORDER_CREATED)
                    .event(OrderEventType.ORDER_CREATED)
                    .action(orderCreated())
                    .and()
                    .withExternal()
                    .source(OrderStatus.ORDER_CREATED)
                    .target(OrderStatus.ACCOUNT_CONNECTED)
                    .event(OrderEventType.ACCOUNT_CONNECTED)
                    .action(accountConnected())
                    .and()
                    .withExternal()
                    .source(OrderStatus.ACCOUNT_CONNECTED)
                    .target(OrderStatus.RESERVATION_PENDING)
                    .event(OrderEventType.RESERVATION_PENDING)
                    .action(reservationPending())
                    .and()
                    .withExternal()
                    .source(OrderStatus.RESERVATION_PENDING)
                    .target(OrderStatus.RESERVATION_SUCCEEDED)
                    .event(OrderEventType.RESERVATION_SUCCEEDED)
                    .action(reservationSucceeded())
                    .and()
                    .withExternal()
                    .source(OrderStatus.RESERVATION_PENDING)
                    .target(OrderStatus.RESERVATION_FAILED)
                    .event(OrderEventType.RESERVATION_FAILED)
                    .action(reservationFailed())
                    .and()
                    .withExternal()
                    .source(OrderStatus.ACCOUNT_CONNECTED)
                    .target(OrderStatus.PAYMENT_CREATED)
                    .event(OrderEventType.PAYMENT_CREATED)
                    .action(paymentCreated())
                    .and()
                    .withExternal()
                    .source(OrderStatus.PAYMENT_CREATED)
                    .target(OrderStatus.PAYMENT_CONNECTED)
                    .event(OrderEventType.PAYMENT_CONNECTED)
                    .action(paymentConnected())
                    .and()
                    .withExternal()
                    .source(OrderStatus.PAYMENT_CONNECTED)
                    .target(OrderStatus.PAYMENT_PENDING)
                    .event(OrderEventType.PAYMENT_PENDING)
                    .action(paymentPending())
                    .and()
                    .withExternal()
                    .source(OrderStatus.PAYMENT_PENDING)
                    .target(OrderStatus.PAYMENT_SUCCEEDED)
                    .event(OrderEventType.PAYMENT_SUCCEEDED)
                    .action(paymentSucceeded())
                    .and()
                    .withExternal()
                    .source(OrderStatus.PAYMENT_PENDING)
                    .target(OrderStatus.PAYMENT_FAILED)
                    .event(OrderEventType.PAYMENT_FAILED)
                    .action(paymentFailed());
        } catch (Exception e) {
            throw new RuntimeException("Could not configure state machine transitions", e);
        }
    }

    @Bean
    public Action<OrderStatus, OrderEventType> orderCreated() {
        return context -> applyEvent(context,
                new OrderCreated(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("order").getHref());
                    // Get the account resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("order").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Order.class)
                            .getBody();
                }));
    }

    @Bean
    public Action<OrderStatus, OrderEventType> paymentPending() {
        return context -> applyEvent(context,
                new PaymentPending(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("order").getHref());
                    // Get the account resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("order").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Order.class)
                            .getBody();
                }));
    }

    @Bean
    public Action<OrderStatus, OrderEventType> reservationPending() {
        return context -> applyEvent(context,
                new ReservationPending(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("order").getHref());
                    // Get the account resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("order").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Order.class)
                            .getBody();
                }));
    }

    @Bean
    public Action<OrderStatus, OrderEventType> paymentFailed() {
        return context -> applyEvent(context,
                new PaymentFailed(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("order").getHref());
                    // Get the account resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("order").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Order.class)
                            .getBody();
                }));
    }

    @Bean
    public Action<OrderStatus, OrderEventType> paymentSucceeded() {
        return context -> applyEvent(context,
                new PaymentSucceeded(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("order").getHref());
                    // Get the account resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("order").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Order.class)
                            .getBody();
                }));
    }

    @Bean
    public Action<OrderStatus, OrderEventType> paymentConnected() {
        return context -> applyEvent(context,
                new PaymentConnected(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("order").getHref());
                    // Get the account resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("order").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Order.class)
                            .getBody();
                }));
    }

    @Bean
    public Action<OrderStatus, OrderEventType> paymentCreated() {
        return context -> applyEvent(context,
                new PaymentCreated(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("order").getHref());
                    // Get the account resource for the event
                    Traverson paymentResource = new Traverson(
                            URI.create(event.getLink("payment").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    Traverson orderResource = new Traverson(
                            URI.create(event.getLink("order").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    Payment payment = paymentResource.follow("self")
                            .toEntity(Payment.class)
                            .getBody();

                    Order order = orderResource.follow("self")
                            .toEntity(Order.class)
                            .getBody();

                    Map<String, Object> template = new HashMap<String, Object>();
                    template.put("paymentId", payment.getPaymentId());
                    return orderResource.follow("commands", "connectPayment")
                            .withTemplateParameters(template)
                            .toObject(Order.class);

                }));
    }

    @Bean
    public Action<OrderStatus, OrderEventType> reservationSucceeded() {
        return context -> applyEvent(context,
                new ReservationSucceeded(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("order").getHref());
                    // Get the account resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("order").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Order.class)
                            .getBody();
                }));
    }

    @Bean
    public Action<OrderStatus, OrderEventType> reservationFailed() {
        return context -> applyEvent(context,
                new ReservationSucceeded(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("order").getHref());
                    // Get the account resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("order").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Order.class)
                            .getBody();
                }));
    }

    @Bean
    public Action<OrderStatus, OrderEventType> accountConnected() {
        return context -> applyEvent(context,
                new ReservationFailed(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("order").getHref());
                    // Get the account resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("order").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Order.class)
                            .getBody();
                }));
    }

    /**
     * Functions are mapped to actions that are triggered during the replication of a state machine. Functions
     * should only be executed after the state machine has completed replication. This method checks the state
     * context of the machine for an {@link OrderEvent}, which signals that the state machine is finished
     * replication.
     * <p>
     * The {@link OrderFunction} argument is only applied if an {@link OrderEvent} is provided as a
     * message header in the {@link StateContext}.
     *
     * @param context       is the state machine context that may include an {@link OrderEvent}
     * @param orderFunction is the order function to apply after the state machine has completed replication
     * @return an {@link OrderEvent} only if this event has not yet been processed, otherwise returns null
     */
    private OrderEvent applyEvent(StateContext<OrderStatus, OrderEventType> context,
                                  OrderFunction orderFunction) {
        OrderEvent orderEvent = null;

        // Log out the progress of the state machine replication
        log.info("Replicate event: " + context.getMessage().getPayload());

        // The machine is finished replicating when an OrderEvent is found in the message header
        if (context.getMessageHeader("event") != null) {
            orderEvent = (OrderEvent) context.getMessageHeader("event");
            log.info("State machine replicated: " + orderEvent.getType());

            // Apply the provided function to the OrderEvent
            orderFunction.apply(orderEvent);
        }

        return orderEvent;
    }
}

