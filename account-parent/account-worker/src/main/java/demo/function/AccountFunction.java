package demo.function;

import demo.account.AccountStatus;
import demo.event.AccountEvent;
import demo.event.AccountEventType;
import org.apache.log4j.Logger;
import org.springframework.statemachine.StateContext;

import java.util.function.Consumer;

/**
 * The {@link AccountFunction} is an abstraction used to map actions that are triggered by
 * state transitions on a {@link demo.account.Account} resource on to a function. Mapped functions
 * can take multiple forms and reside either remotely or locally on the classpath of this application.
 *
 * @author kbastani
 */
public abstract class AccountFunction {

    final private Logger log = Logger.getLogger(AccountFunction.class);
    final private StateContext<AccountStatus, AccountEventType> context;
    final private Consumer<AccountEvent> lambda;

    /**
     * Create a new instance of a class that extends {@link AccountFunction}, supplying
     * a state context and a lambda function used to apply {@link AccountEvent} to a provided
     * action.
     *
     * @param context is the {@link StateContext} for a replicated state machine
     * @param lambda  is the lambda function describing an action that consumes an {@link AccountEvent}
     */
    public AccountFunction(StateContext<AccountStatus, AccountEventType> context,
                           Consumer<AccountEvent> lambda) {
        this.context = context;
        this.lambda = lambda;
    }

    /**
     * Apply an {@link AccountEvent} to the lambda function that was provided through the
     * constructor of this {@link AccountFunction}.
     *
     * @param event is the {@link AccountEvent} to apply to the lambda function
     */
    public void apply(AccountEvent event) {
        // Execute the lambda function
        lambda.accept(event);
    }
}
