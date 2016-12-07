package demo.command;

import demo.account.AccountStatus;
import demo.event.AccountEventType;
import org.springframework.statemachine.StateContext;

public class ArchiveAccountCommand extends AccountCommand {
    public ArchiveAccountCommand(StateContext<AccountStatus, AccountEventType> context) {
        super(context);
    }
}
