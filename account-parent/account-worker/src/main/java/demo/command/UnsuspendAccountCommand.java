package demo.command;

import demo.account.AccountStatus;
import demo.event.AccountEventType;
import org.springframework.statemachine.StateContext;

public class UnsuspendAccountCommand extends AccountCommand {
    public UnsuspendAccountCommand(StateContext<AccountStatus, AccountEventType> context) {
        super(context);
    }
}
