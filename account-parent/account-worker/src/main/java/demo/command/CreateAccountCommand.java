package demo.command;

import demo.account.Account;
import demo.account.AccountStatus;
import demo.event.AccountEvent;
import demo.event.AccountEventType;
import org.apache.log4j.Logger;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.http.RequestEntity;
import org.springframework.statemachine.StateContext;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

public class CreateAccountCommand extends AccountCommand {

    final private Logger log = Logger.getLogger(CreateAccountCommand.class);

    public CreateAccountCommand(StateContext<AccountStatus, AccountEventType> context) {
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

        // Create a traverson for the root account
        Traverson traverson = new Traverson(
                new URI(event.getLink("account").getHref()),
                MediaTypes.HAL_JSON
        );

        // Get the account resource attached to the event
        Account account = traverson.follow("self")
                .toEntity(Account.class)
                .getBody();

        // Set the account to a pending state
        account = setAccountPendingStatus(event, account);

        // The account can only be confirmed if it is in a pending state
        if (account.getStatus() == AccountStatus.ACCOUNT_PENDING) {
            // Traverse to the confirm account command
            account = traverson.follow("commands")
                    .follow("confirm")
                    .toEntity(Account.class)
                    .getBody();

            log.info("Account confirmed: " + account);
        }
    }

    /**
     * Set the {@link Account} resource to a pending state.
     *
     * @param event   is the {@link AccountEvent} for this context
     * @param account is the {@link Account} attached to the {@link AccountEvent} resource
     * @return an {@link Account} with its updated state set to pending
     * @throws URISyntaxException is thrown if the {@link Account} hypermedia link cannot be parsed
     */
    private Account setAccountPendingStatus(AccountEvent event, Account account) throws URISyntaxException {
        // Set the account status to pending
        account.setStatus(AccountStatus.ACCOUNT_PENDING);
        RestTemplate restTemplate = new RestTemplate();

        // Create a new request entity
        RequestEntity<Account> requestEntity = RequestEntity.put(
                new URI(event.getLink("account").getHref()))
                .contentType(MediaTypes.HAL_JSON)
                .body(account);

        // Update the account entity's status
        account = restTemplate.exchange(requestEntity, Account.class).getBody();

        return account;
    }
}
