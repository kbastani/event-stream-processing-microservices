package demo.command;

import demo.account.AccountStatus;
import demo.event.AccountEventType;
import org.springframework.statemachine.StateContext;

public class SuspendAccountCommand extends AccountCommand {
    public SuspendAccountCommand(StateContext<AccountStatus, AccountEventType> context) {
        super(context);
    }
}
