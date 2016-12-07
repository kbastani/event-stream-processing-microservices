package demo.command;

import demo.account.AccountEventStatus;
import demo.account.AccountEventType;
import org.springframework.statemachine.StateContext;

public class ActivateAccountCommand extends AccountCommand {
    public ActivateAccountCommand(StateContext<AccountEventStatus, AccountEventType> context) {
        super(context);
    }
}
