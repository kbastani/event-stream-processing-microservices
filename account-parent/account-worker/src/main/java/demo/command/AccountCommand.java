package demo.command;

import demo.account.AccountStatus;
import demo.event.AccountEventType;
import demo.event.AccountEvent;
import org.apache.log4j.Logger;
import org.springframework.statemachine.StateContext;

public abstract class AccountCommand {

    final private Logger log = Logger.getLogger(AccountCommand.class);

    final private StateContext<AccountStatus, AccountEventType> context;

    public AccountCommand(StateContext<AccountStatus, AccountEventType> context) {
        this.context = context;
    }

    public void apply(AccountEvent event) throws Exception {
        log.info("Command triggered " + event.getType() + ": " + event.getLink("account"));
    }
}
