package demo.function;

import demo.account.Account;
import demo.account.AccountStatus;
import demo.event.AccountEvent;
import demo.event.AccountEventType;
import org.apache.log4j.Logger;
import org.springframework.statemachine.StateContext;

import java.util.function.Function;

/**
 * The {@link AccountFunction} is an abstraction used to map actions that are triggered by
 * state transitions on a {@link demo.account.Account} resource on to a function. Mapped functions
 * can take multiple forms and reside either remotely or locally on the classpath of this application.
 *
 * @author kbastani
 */
public class UnsuspendAccount extends AccountFunction {

    final private Logger log = Logger.getLogger(UnsuspendAccount.class);

    public UnsuspendAccount(StateContext<AccountStatus, AccountEventType> context, Function<AccountEvent, Account> lambda) {
        super(context, lambda);
    }

    /**
     * Apply an {@link AccountEvent} to the lambda function that was provided through the
     * constructor of this {@link AccountFunction}.
     *
     * @param event is the {@link AccountEvent} to apply to the lambda function
     */
    @Override
    public Account apply(AccountEvent event) {
        log.info("Executing workflow for a unsuspended account...");
        return super.apply(event);
    }
}
