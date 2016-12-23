package demo.config;

import demo.payment.Payment;
import demo.payment.PaymentStatus;
import demo.event.PaymentEvent;
import demo.event.PaymentEventType;
import demo.function.*;
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
 * A state machine provides a robust declarative language for describing the state of an {@link Payment}
 * resource given a sequence of ordered {@link demo.event.PaymentEvents}. When an event is received
 * in {@link demo.event.PaymentEventStream}, an in-memory state machine is fully replicated given the
 * {@link demo.event.PaymentEvents} attached to an {@link Payment} resource.
 *
 * @author kbastani
 */
@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<PaymentStatus, PaymentEventType> {

    final private Logger log = Logger.getLogger(StateMachineConfig.class);

    /**
     * Configures the initial conditions of a new in-memory {@link StateMachine} for {@link Payment}.
     *
     * @param states is the {@link StateMachineStateConfigurer} used to describe the initial condition
     */
    @Override
    public void configure(StateMachineStateConfigurer<PaymentStatus, PaymentEventType> states) {
        try {
            // Describe the initial condition of the payment state machine
            states.withStates()
                    .initial(PaymentStatus.PAYMENT_CREATED)
                    .states(EnumSet.allOf(PaymentStatus.class));
        } catch (Exception e) {
            throw new RuntimeException("State machine configuration failed", e);
        }
    }

    /**
     * Configures the {@link StateMachine} that describes how {@link PaymentEventType} drives the state
     * of an {@link Payment}. Events are applied as transitions from a source {@link PaymentStatus} to
     * a target {@link PaymentStatus}. An {@link Action} is attached to each transition, which maps to a
     * function that is executed in the context of an {@link PaymentEvent}.
     *
     * @param transitions is the {@link StateMachineTransitionConfigurer} used to describe state transitions
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentStatus, PaymentEventType> transitions) {
        try {
            // Describe state machine transitions for payments
            transitions.withExternal()
                    .source(PaymentStatus.PAYMENT_CREATED)
                    .target(PaymentStatus.PAYMENT_CREATED)
                    .event(PaymentEventType.PAYMENT_CREATED)
                    .action(paymentCreated())
                    .and()
                    .withExternal()
                    .source(PaymentStatus.PAYMENT_CREATED)
                    .target(PaymentStatus.PAYMENT_PENDING)
                    .event(PaymentEventType.PAYMENT_PENDING)
                    .action(paymentPending())
                    .and()
                    .withExternal()
                    .source(PaymentStatus.PAYMENT_PENDING)
                    .target(PaymentStatus.PAYMENT_PROCESSED)
                    .event(PaymentEventType.PAYMENT_PROCESSED)
                    .action(paymentProcessed())
                    .and()
                    .withExternal()
                    .source(PaymentStatus.PAYMENT_PROCESSED)
                    .target(PaymentStatus.PAYMENT_SUCCEEDED)
                    .event(PaymentEventType.PAYMENT_SUCCEEDED)
                    .action(paymentSucceeded())
                    .and()
                    .withExternal()
                    .source(PaymentStatus.PAYMENT_PROCESSED)
                    .target(PaymentStatus.PAYMENT_FAILED)
                    .event(PaymentEventType.PAYMENT_FAILED)
                    .action(paymentFailed());
        } catch (Exception e) {
            throw new RuntimeException("Could not configure state machine transitions", e);
        }
    }

    /**
     * Functions are mapped to actions that are triggered during the replication of a state machine. Functions
     * should only be executed after the state machine has completed replication. This method checks the state
     * context of the machine for an {@link PaymentEvent}, which signals that the state machine is finished
     * replication.
     * <p>
     * The {@link PaymentFunction} argument is only applied if an {@link PaymentEvent} is provided as a
     * message header in the {@link StateContext}.
     *
     * @param context         is the state machine context that may include an {@link PaymentEvent}
     * @param paymentFunction is the payment function to apply after the state machine has completed replication
     * @return an {@link PaymentEvent} only if this event has not yet been processed, otherwise returns null
     */
    private PaymentEvent applyEvent(StateContext<PaymentStatus, PaymentEventType> context, PaymentFunction paymentFunction) {
        PaymentEvent paymentEvent = null;

        // Log out the progress of the state machine replication
        log.info("Replicate event: " + context.getMessage().getPayload());

        // The machine is finished replicating when an PaymentEvent is found in the message header
        if (context.getMessageHeader("event") != null) {
            paymentEvent = (PaymentEvent) context.getMessageHeader("event");
            log.info("State machine replicated: " + paymentEvent.getType());

            // Apply the provided function to the PaymentEvent
            paymentFunction.apply(paymentEvent);
        }

        return paymentEvent;
    }

    @Bean
    public Action<PaymentStatus, PaymentEventType> paymentCreated() {
        return context -> applyEvent(context,
                new PaymentCreated(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("payment").getHref());
                    // Get the payment resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("payment").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Payment.class)
                            .getBody();
                }));
    }

    @Bean
    public Action<PaymentStatus, PaymentEventType> paymentPending() {
        return context -> applyEvent(context,
                new PaymentPending(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("payment").getHref());
                    // Get the payment resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("payment").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Payment.class)
                            .getBody();
                }));
    }

    @Bean
    public Action<PaymentStatus, PaymentEventType> paymentProcessed() {
        return context -> applyEvent(context,
                new PaymentProcessed(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("payment").getHref());
                    // Get the payment resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("payment").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Payment.class)
                            .getBody();
                }));
    }

    @Bean
    public Action<PaymentStatus, PaymentEventType> paymentSucceeded() {
        return context -> applyEvent(context,
                new PaymentSucceeded(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("payment").getHref());
                    // Get the payment resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("payment").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Payment.class)
                            .getBody();
                }));
    }

    @Bean
    public Action<PaymentStatus, PaymentEventType> paymentFailed() {
        return context -> applyEvent(context,
                new PaymentFailed(context, event -> {
                    log.info(event.getType() + ": " + event.getLink("payment").getHref());
                    // Get the payment resource for the event
                    Traverson traverson = new Traverson(
                            URI.create(event.getLink("payment").getHref()),
                            MediaTypes.HAL_JSON
                    );

                    return traverson.follow("self")
                            .toEntity(Payment.class)
                            .getBody();
                }));
    }
}

