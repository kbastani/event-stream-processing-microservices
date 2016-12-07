package demo.command;

import demo.account.AccountEventStatus;
import demo.account.AccountEventType;
import org.springframework.statemachine.StateContext;

public class SuspendAccountCommand extends AccountCommand {
    public SuspendAccountCommand(StateContext<AccountEventStatus, AccountEventType> context) {
        super(context);
    }
}
