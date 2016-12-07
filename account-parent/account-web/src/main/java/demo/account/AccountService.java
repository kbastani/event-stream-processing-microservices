package demo.account;

import demo.event.*;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Objects;

import static demo.account.AccountStatus.*;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * The {@link AccountService} provides transactional support for managing {@link Account}
 * entities. This service also provides event sourcing support for {@link AccountEvent}.
 * Events can be appended to an {@link Account}, which contains a append-only log of
 * actions that can be used to support remediation for distributed transactions that encountered
 * a partial failure.
 *
 * @author kbastani
 */
@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final EventService eventService;

    public AccountService(AccountRepository accountRepository, EventService eventService) {
        this.accountRepository = accountRepository;
        this.eventService = eventService;
    }

    /**
     * Retrieves a hypermedia resource for {@link Account} with the specified identifier.
     *
     * @param id is the unique identifier for looking up the {@link Account} entity
     * @return a hypermedia resource for the fetched {@link Account}
     */
    public Resource<Account> getAccountResource(Long id) {
        Resource<Account> accountResource = null;

        // Get the account for the provided id
        Account account = getAccount(id);

        // If the account exists, wrap the hypermedia response
        if (account != null)
            accountResource = getAccountResource(account);


        return accountResource;
    }

    /**
     * Creates a new {@link Account} entity and persists the result to the repository.
     *
     * @param account is the {@link Account} model used to create a new account
     * @return a hypermedia resource for the newly created {@link Account}
     */
    public Resource<Account> createAccountResource(Account account) {
        Assert.notNull(account, "Account body must not be null");
        Assert.notNull(account.getUserId(), "UserId is required");
        Assert.notNull(account.getAccountNumber(), "AccountNumber is required");
        Assert.notNull(account.getDefaultAccount(), "DefaultAccount is required");

        // Create the new account
        account = createAccount(account);

        return getAccountResource(account);
    }

    /**
     * Update a {@link Account} entity for the provided identifier.
     *
     * @param id      is the unique identifier for the {@link Account} update
     * @param account is the entity representation containing any updated {@link Account} fields
     * @return a hypermedia resource for the updated {@link Account}
     */
    public Resource<Account> updateAccountResource(Long id, Account account) {
        return getAccountResource(updateAccount(id, account));
    }

    /**
     * Appends an {@link AccountEvent} domain event to the event log of the {@link Account}
     * aggregate with the specified accountId.
     *
     * @param accountId is the unique identifier for the {@link Account}
     * @param event     is the {@link AccountEvent} that attempts to alter the state of the {@link Account}
     * @return a hypermedia resource for the newly appended {@link AccountEvent}
     */
    public Resource<AccountEvent> appendEventResource(Long accountId, AccountEvent event) {
        Resource<AccountEvent> eventResource = null;

        event = appendEvent(accountId, event);

        if (event != null) {
            eventResource = new Resource<>(event,
                    linkTo(EventController.class)
                            .slash("events")
                            .slash(event.getEventId())
                            .withSelfRel(),
                    linkTo(AccountController.class)
                            .slash("accounts")
                            .slash(accountId)
                            .withRel("account")
            );
        }

        return eventResource;
    }

    /**
     * Apply an {@link AccountCommand} to the {@link Account} with a specified identifier.
     *
     * @param id             is the unique identifier of the {@link Account}
     * @param accountCommand is the command to apply to the {@link Account}
     * @return a hypermedia resource containing the updated {@link Account}
     */
    public Resource<Account> applyCommand(Long id, AccountCommand accountCommand) {
        Resource<Account> account = getAccountResource(id);

        Assert.notNull(account, "The account for the supplied id could not be found");

        AccountStatus status = account.getContent().getStatus();

        switch (accountCommand) {
            case CONFIRM_ACCOUNT:
                Assert.isTrue(status == ACCOUNT_PENDING, "The account has already been confirmed");

                // Confirm the account
                Account updateAccount = account.getContent();
                updateAccount.setStatus(ACCOUNT_CONFIRMED);
                account = updateAccountResource(id, updateAccount);
                appendEvent(id, new AccountEvent(AccountEventType.ACCOUNT_CONFIRMED));
                break;
            case ACTIVATE_ACCOUNT:
                Assert.isTrue(status != ACCOUNT_ACTIVE, "The account is already active");
                Assert.isTrue(Arrays.asList(ACCOUNT_CONFIRMED, ACCOUNT_SUSPENDED, ACCOUNT_ARCHIVED)
                        .contains(status), "The account cannot be activated");

                // Activate the account
                account.getContent().setStatus(ACCOUNT_ACTIVE);
                account = updateAccountResource(id, account.getContent());
                appendEvent(id, new AccountEvent(AccountEventType.ACCOUNT_ACTIVATED));
                break;
            case SUSPEND_ACCOUNT:
                Assert.isTrue(status == ACCOUNT_ACTIVE, "An inactive account cannot be suspended");

                // Suspend the account
                account.getContent().setStatus(ACCOUNT_SUSPENDED);
                account = updateAccountResource(id, account.getContent());
                appendEvent(id, new AccountEvent(AccountEventType.ACCOUNT_SUSPENDED));
                break;
            case ARCHIVE_ACCOUNT:
                Assert.isTrue(status == ACCOUNT_ACTIVE, "An inactive account cannot be archived");

                // Archive the account
                account.getContent().setStatus(ACCOUNT_ARCHIVED);
                account = updateAccountResource(id, account.getContent());
                appendEvent(id, new AccountEvent(AccountEventType.ACCOUNT_ARCHIVED));
                break;
            default:
                Assert.notNull(accountCommand,
                        "The provided command cannot be applied to this account in its current state");
        }

        return account;
    }

    /**
     * Get the {@link AccountCommand} hypermedia resource that lists the available commands that can be applied
     * to an {@link Account} entity.
     *
     * @param id is the {@link Account} identifier to provide command links for
     * @return an {@link AccountCommandsResource} with a collection of embedded command links
     */
    public AccountCommandsResource getCommandsResource(Long id) {
        // Get the account resource for the identifier
        Resource<Account> accountResource = getAccountResource(id);

        // Create a new account commands hypermedia resource
        AccountCommandsResource commandResource = new AccountCommandsResource();

        // Add account command hypermedia links
        if (accountResource != null) {
            commandResource.add(
                    getCommandLinkBuilder(id)
                            .slash("confirm")
                            .withRel("confirm"),
                    getCommandLinkBuilder(id)
                            .slash("activate")
                            .withRel("activate"),
                    getCommandLinkBuilder(id)
                            .slash("suspend")
                            .withRel("suspend"),
                    getCommandLinkBuilder(id)
                            .slash("archive")
                            .withRel("archive")
            );
        }

        return commandResource;
    }

    /**
     * Get {@link AccountEvents} for the supplied {@link Account} identifier.
     *
     * @param id is the unique identifier of the {@link Account}
     * @return a list of {@link AccountEvent} wrapped in a hypermedia {@link AccountEvents} resource
     */
    public AccountEvents getAccountEventResources(Long id) {
        return eventService.getEvents(id);
    }

    /**
     * Generate a {@link LinkBuilder} for generating the {@link AccountCommandsResource}.
     *
     * @param id is the unique identifier for a {@link Account}
     * @return a {@link LinkBuilder} for the {@link AccountCommandsResource}
     */
    private LinkBuilder getCommandLinkBuilder(Long id) {
        return linkTo(AccountController.class)
                .slash("accounts")
                .slash(id)
                .slash("commands");
    }

    /**
     * Get a hypermedia enriched {@link Account} entity.
     *
     * @param account is the {@link Account} to enrich with hypermedia links
     * @return is a hypermedia enriched resource for the supplied {@link Account} entity
     */
    private Resource<Account> getAccountResource(Account account) {
        Resource<Account> accountResource;

        // Prepare hypermedia response
        accountResource = new Resource<>(account,
                linkTo(AccountController.class)
                        .slash("accounts")
                        .slash(account.getAccountId())
                        .withSelfRel(),
                linkTo(AccountController.class)
                        .slash("accounts")
                        .slash(account.getAccountId())
                        .slash("events")
                        .withRel("events"),
                getCommandLinkBuilder(account.getAccountId())
                        .withRel("commands")
        );

        return accountResource;
    }

    /**
     * Get an {@link Account} entity for the supplied identifier.
     *
     * @param id is the unique identifier of a {@link Account} entity
     * @return an {@link Account} entity
     */
    private Account getAccount(Long id) {
        return accountRepository.findOne(id);
    }

    /**
     * Create a new {@link Account} entity.
     *
     * @param account is the {@link Account} to create
     * @return the newly created {@link Account}
     */
    private Account createAccount(Account account) {
        // Assert for uniqueness constraint
        Assert.isNull(accountRepository.findAccountByUserId(account.getUserId()),
                "An account with the supplied userId already exists");
        Assert.isNull(accountRepository.findAccountByAccountNumber(account.getAccountNumber()),
                "An account with the supplied account number already exists");

        // Save the account to the repository
        account = accountRepository.save(account);

        // Trigger the account creation event
        appendEventResource(account.getAccountId(),
                new AccountEvent(AccountEventType.ACCOUNT_CREATED));

        return account;
    }

    /**
     * Update an {@link Account} entity with the supplied identifier.
     *
     * @param id      is the unique identifier of the {@link Account} entity
     * @param account is the {@link Account} containing updated fields
     * @return the updated {@link Account} entity
     */
    private Account updateAccount(Long id, Account account) {
        Assert.notNull(id, "Account id must be present in the resource URL");
        Assert.notNull(account, "Account request body cannot be null");

        if(account.getAccountId() != null) {
            Assert.isTrue(Objects.equals(id, account.getAccountId()),
                    "The account id in the request body must match the resource URL");
        } else {
            account.setAccountId(id);
        }

        Account currentAccount = getAccount(id);
        currentAccount.setStatus(account.getStatus());
        currentAccount.setDefaultAccount(account.getDefaultAccount());
        currentAccount.setAccountNumber(account.getAccountNumber());
        currentAccount.setUserId(account.getUserId());

        return accountRepository.save(currentAccount);
    }

    /**
     * Append a new {@link AccountEvent} to the {@link Account} reference for the supplied identifier.
     *
     * @param accountId is the unique identifier for the {@link Account}
     * @param event     is the {@link AccountEvent} to append to the {@link Account} entity
     * @return the newly appended {@link AccountEvent}
     */
    private AccountEvent appendEvent(Long accountId, AccountEvent event) {
        Account account = getAccount(accountId);
        Assert.notNull(account, "The account with the supplied id does not exist");
        event.setAccount(account);
        event = eventService.createEvent(event).getContent();
        account.getEvents().add(event);
        accountRepository.save(account);
        return event;
    }
}
