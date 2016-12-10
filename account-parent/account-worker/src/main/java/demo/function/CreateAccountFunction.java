package demo.function;

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
import java.util.function.Consumer;

/**
 * The {@link AccountFunction} is an abstraction used to map actions that are triggered by
 * state transitions on a {@link demo.account.Account} resource on to a function. Mapped functions
 * can take multiple forms and reside either remotely or locally on the classpath of this application.
 *
 * @author kbastani
 */
public class CreateAccountFunction extends AccountFunction {

    final private Logger log = Logger.getLogger(CreateAccountFunction.class);

    public CreateAccountFunction(StateContext<AccountStatus, AccountEventType> context) {
        super(context, null);
    }

    public CreateAccountFunction(StateContext<AccountStatus, AccountEventType> context,
                                 Consumer<AccountEvent> function) {
        super(context, function);
    }

    /**
     * Applies the {@link AccountEvent} to the {@link Account} aggregate.
     *
     * @param event is the {@link AccountEvent} for this context
     */
    @Override
    public void apply(AccountEvent event) {
        log.info("Executing workflow for a created account...");

        // Create a traverson for the root account
        Traverson traverson = new Traverson(
                URI.create(event.getLink("account").getHref()),
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

            log.info(event.getType() + ": " +
                    event.getLink("account").getHref());
        }
    }

    /**
     * Set the {@link Account} resource to a pending state.
     *
     * @param event   is the {@link AccountEvent} for this context
     * @param account is the {@link Account} attached to the {@link AccountEvent} resource
     * @return an {@link Account} with its updated state set to pending
     */
    private Account setAccountPendingStatus(AccountEvent event, Account account) {
        // Set the account status to pending
        account.setStatus(AccountStatus.ACCOUNT_PENDING);
        RestTemplate restTemplate = new RestTemplate();

        // Create a new request entity
        RequestEntity<Account> requestEntity = RequestEntity.put(
                URI.create(event.getLink("account").getHref()))
                .contentType(MediaTypes.HAL_JSON)
                .body(account);

        // Update the account entity's status
        account = restTemplate.exchange(requestEntity, Account.class).getBody();

        return account;
    }
}
