package demo.reservation.function;

import demo.reservation.domain.Reservation;
import demo.reservation.domain.ReservationStatus;
import demo.reservation.event.ReservationEvent;
import demo.reservation.event.ReservationEventType;
import org.apache.log4j.Logger;
import org.springframework.statemachine.StateContext;

import java.util.function.Function;

/**
 * The {@link ReservationFunction} is an abstraction used to map actions that are triggered by
 * state transitions on a {@link Reservation} resource on to a function. Mapped functions
 * can take multiple forms and reside either remotely or locally on the classpath of this application.
 *
 * @author kbastani
 */
public abstract class ReservationFunction {

    final private Logger log = Logger.getLogger(ReservationFunction.class);
    final protected StateContext<ReservationStatus, ReservationEventType> context;
    final protected Function<ReservationEvent, Reservation> lambda;

    /**
     * Create a new instance of a class that extends {@link ReservationFunction}, supplying
     * a state context and a lambda function used to apply {@link ReservationEvent} to a provided
     * action.
     *
     * @param context is the {@link StateContext} for a replicated state machine
     * @param lambda  is the lambda function describing an action that consumes an {@link ReservationEvent}
     */
    public ReservationFunction(StateContext<ReservationStatus, ReservationEventType> context,
                         Function<ReservationEvent, Reservation> lambda) {
        this.context = context;
        this.lambda = lambda;
    }

    /**
     * Apply an {@link ReservationEvent} to the lambda function that was provided through the
     * constructor of this {@link ReservationFunction}.
     *
     * @param event is the {@link ReservationEvent} to apply to the lambda function
     */
    public Reservation apply(ReservationEvent event) {
        // Execute the lambda function
        Reservation result = lambda.apply(event);
        context.getExtendedState().getVariables().put("reservation", result);
        log.info("Reservation function: " + event.getType());
        return result;
    }
}
