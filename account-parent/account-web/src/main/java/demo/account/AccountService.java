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

    public Resource<Account> getAccountResource(Long id) {
        Resource<Account> accountResource = null;
        Account account = getAccount(id);

        if (account != null) {
            accountResource = getAccountResource(account);
        }

        return accountResource;
    }

    private Resource<Account> getAccountResource(Account account) {
        Resource<Account> accountResource;
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

    public Resource<Account> createAccountResource(Account account) {
        return getAccountResource(createAccount(account));
    }

    private Account createAccount(Account account) {
        account = accountRepository.save(account);
        return account;
    }

    public Resource<Account> updateAccountResource(Long id, Account account) {
        return getAccountResource(updateAccount(id, account));
    }

    private Account updateAccount(Long id, Account account) {
        Assert.notNull(id);
        Assert.notNull(account);
        Assert.isTrue(Objects.equals(id, account.getId()));
        return accountRepository.save(account);
    }

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
