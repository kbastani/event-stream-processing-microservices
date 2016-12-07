package demo.command;

import demo.account.AccountStatus;
import demo.event.AccountEventType;
import org.springframework.statemachine.StateContext;

public class ActivateAccountCommand extends AccountCommand {
    public ActivateAccountCommand(StateContext<AccountStatus, AccountEventType> context) {
        super(context);
    }
}
