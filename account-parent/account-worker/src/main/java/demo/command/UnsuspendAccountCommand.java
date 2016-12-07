package demo.command;

import demo.account.AccountEventStatus;
import demo.account.AccountEventType;
import org.springframework.statemachine.StateContext;

public class UnsuspendAccountCommand extends AccountCommand {
    public UnsuspendAccountCommand(StateContext<AccountEventStatus, AccountEventType> context) {
        super(context);
    }
}
