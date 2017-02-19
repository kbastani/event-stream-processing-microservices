package demo.account.domain;

import demo.account.event.AccountEvent;
import demo.account.event.AccountEventType;
import demo.account.repository.AccountRepository;
import demo.domain.Service;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

@org.springframework.stereotype.Service
public class AccountService extends Service<Account, Long> {

    private final Logger log = Logger.getLogger(AccountService.class);
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Register a new {@link Account} and handle a synchronous event flow for account creation
     *
     * @param account is the {@link Account} to create
     * @return the created account
     * @throws IllegalStateException if the event flow fails
     */
    public Account registerAccount(Account account) throws IllegalStateException {
        account = create(account);

        try {
            // Handle a synchronous event flow
            account.sendAsyncEvent(new AccountEvent(AccountEventType.ACCOUNT_CREATED, account));
        } catch (Exception ex) {
            log.error("Account registration failed", ex);
            // Rollback the account creation
            delete(account.getIdentity());
            throw ex;
        }

        // Return the result
        return account;
    }

    /**
     * Create a new {@link Account} entity.
     *
     * @param account is the {@link Account} to create
     * @return the newly created {@link Account}
     */
    public Account create(Account account) {

        // Assert for uniqueness constraint
        Assert.isNull(accountRepository.findAccountByEmail(account.getEmail()),
                "An account with the supplied email already exists");

        // Save the account to the repository
        account = accountRepository.saveAndFlush(account);

        return account;
    }

    /**
     * Get an {@link Account} entity for the supplied identifier.
     *
     * @param id is the unique identifier of a {@link Account} entity
     * @return an {@link Account} entity
     */
    public Account get(Long id) {
        return accountRepository.findOne(id);
    }

    /**
     * Update an {@link Account} entity with the supplied identifier.
     *
     * @param account is the {@link Account} containing updated fields
     * @return the updated {@link Account} entity
     */
    @Override
    public Account update(Account account) {
        Assert.notNull(account.getIdentity(), "Account id must be present in the resource URL");
        Assert.notNull(account, "Account request body cannot be null");

        Assert.state(accountRepository.exists(account.getIdentity()),
                "The account with the supplied id does not exist");

        Account currentAccount = get(account.getIdentity());
        currentAccount.setEmail(account.getEmail());
        currentAccount.setFirstName(account.getFirstName());
        currentAccount.setLastName(account.getLastName());
        currentAccount.setStatus(account.getStatus());

        return accountRepository.save(currentAccount);
    }

    /**
     * Delete the {@link Account} with the supplied identifier.
     *
     * @param id is the unique identifier for the {@link Account}
     */
    public boolean delete(Long id) {
        Assert.state(accountRepository.exists(id),
                "The account with the supplied id does not exist");
        this.accountRepository.delete(id);
        return true;
    }
}
