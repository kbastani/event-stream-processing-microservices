package demo.command;

import demo.account.AccountEventStatus;
import demo.account.AccountEventType;
import org.springframework.statemachine.StateContext;

public class UnarchiveAccountCommand extends AccountCommand {
    public UnarchiveAccountCommand(StateContext<AccountEventStatus, AccountEventType> context) {
        super(context);
    }
}
