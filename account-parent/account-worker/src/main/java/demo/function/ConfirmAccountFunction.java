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
public class ConfirmAccountFunction extends AccountFunction {

    final private Logger log = Logger.getLogger(ConfirmAccountFunction.class);

    public ConfirmAccountFunction(StateContext<AccountStatus, AccountEventType> context, Consumer<AccountEvent> lambda) {
        super(context, lambda);
    }

    /**
     * Apply an {@link AccountEvent} to the lambda function that was provided through the
     * constructor of this {@link AccountFunction}.
     *
     * @param event is the {@link AccountEvent} to apply to the lambda function
     */
    @Override
    public void apply(AccountEvent event) {
        log.info("Executing workflow for a confirmed account...");
        super.apply(event);
    }
}
