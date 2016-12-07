package demo.command;

import demo.account.AccountEventStatus;
import demo.account.AccountEventType;
import org.springframework.statemachine.StateContext;

public class ArchiveAccountCommand extends AccountCommand {
    public ArchiveAccountCommand(StateContext<AccountEventStatus, AccountEventType> context) {
        super(context);
    }
}
