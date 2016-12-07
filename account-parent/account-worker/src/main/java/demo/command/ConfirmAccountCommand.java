package demo.command;

import demo.account.Account;
import demo.account.AccountStatus;
import demo.event.AccountEventType;
import demo.event.AccountEvent;
import org.apache.log4j.Logger;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.statemachine.StateContext;

import java.net.URI;

public class ConfirmAccountCommand extends AccountCommand {

    final private Logger log = Logger.getLogger(ConfirmAccountCommand.class);

    public ConfirmAccountCommand(StateContext<AccountStatus, AccountEventType> context) {
        super(context);
    }

    /**
     * Applies the {@link AccountEvent} to the {@link Account} aggregate.
     *
     * @param event is the {@link AccountEvent} for this context
     */
    @Override
    public void apply(AccountEvent event) throws Exception {
        super.apply(event);

        // Create a new hypermedia traversal for the account
        Traverson traverson = new Traverson(
                new URI(event.getLink("account").getHref()),
                MediaTypes.HAL_JSON
        );

        // Traverse to activate the
        Account account = traverson.follow("commands")
                .follow("activate")
                .toEntity(Account.class)
                .getBody();

        log.info("Account activated: " + account);
    }
}
