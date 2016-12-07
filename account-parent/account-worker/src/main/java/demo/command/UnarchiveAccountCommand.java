package demo.command;

import demo.account.AccountStatus;
import demo.event.AccountEventType;
import org.springframework.statemachine.StateContext;

public class UnarchiveAccountCommand extends AccountCommand {
    public UnarchiveAccountCommand(StateContext<AccountStatus, AccountEventType> context) {
        super(context);
    }
}
