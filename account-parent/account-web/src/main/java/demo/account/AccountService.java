package demo.account;

import demo.event.AccountEvent;
import demo.event.EventService;
import org.springframework.data.rest.webmvc.support.RepositoryEntityLinks;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * The {@link AccountService} provides transactional support for managing {@link Account}
 * entities. This service also provides event sourcing support for {@link AccountEvent}.
 * Events can be appended to an {@link Account}, which contains a append-only log of
 * actions that can be used to support remediation for distributed transactions that encountered
 * a partial failure.
 */
@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final EventService eventService;
    private final RepositoryEntityLinks entityLinks;

    public AccountService(AccountRepository accountRepository, EventService eventService,
                          RepositoryEntityLinks entityLinks) {
        this.accountRepository = accountRepository;
        this.eventService = eventService;
        this.entityLinks = entityLinks;
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
                    entityLinks.linkFor(AccountEvent.class, event.getId())
                            .slash(event.getId())
                            .withSelfRel(),
                    entityLinks.linkFor(Account.class, accountId)
                            .slash(accountId)
                            .withRel("account"),
                    entityLinks.linkFor(AccountEvent.class, event.getId())
                            .slash(event.getId())
                            .slash("logs")
                            .withRel("logs")
            );
        }

        return eventResource;
    }

    private Resource<Account> getAccountResource(Account account) {
        Resource<Account> accountResource;

        // Prepare hypermedia response
        accountResource = new Resource<>(account,
                entityLinks.linkFor(Account.class, account.getId())
                        .slash(account.getId())
                        .withSelfRel(),
                entityLinks.linkFor(Account.class, account.getId())
                        .slash(account.getId())
                        .slash("events")
                        .withRel("events")
        );

        return accountResource;
    }

    private Account getAccount(Long id) {
        return accountRepository.findOne(id);
    }

    private Account createAccount(Account account) {
        // Assert for uniqueness constraint
        Assert.isNull(accountRepository.findAccountByUserId(account.getUserId()),
                "An account with the supplied userId already exists");
        Assert.isNull(accountRepository.findAccountByAccountNumber(account.getAccountNumber()),
                "An account with the supplied account number already exists");

        // Save the account to the repository
        account = accountRepository.save(account);

        // Trigger the account creation event
        appendEventResource(account.getId(),
                new AccountEvent(AccountEventType.ACCOUNT_CREATED));

        return account;
    }

    private Account updateAccount(Long id, Account account) {
        Assert.notNull(id);
        Assert.notNull(account);
        Assert.isTrue(Objects.equals(id, account.getId()));
        return accountRepository.save(account);
    }

    private AccountEvent appendEvent(Long accountId, AccountEvent event) {
        Account account = accountRepository.findOne(accountId);
        if (account != null) {
            event.setAccount(account);
            event = eventService.createEvent(event).getContent();
            if (event != null) {
                account.getEvents().add(event);
                accountRepository.save(account);
            }
        } else {
            event = null;
        }

        return event;
    }
}
