package demo.command;

import demo.account.AccountEventStatus;
import demo.account.AccountEventType;
import demo.event.AccountEvent;
import org.apache.log4j.Logger;
import org.springframework.statemachine.StateContext;

public abstract class AccountCommand {

    final private Logger log = Logger.getLogger(AccountCommand.class);

    final private StateContext<AccountEventStatus, AccountEventType> context;

    public AccountCommand(StateContext<AccountEventStatus, AccountEventType> context) {
        this.context = context;
    }

    public void apply(AccountEvent event) throws Exception {
        log.info("Command triggered " + event.getType() + ": " + event.getLink("account"));
    }
}
