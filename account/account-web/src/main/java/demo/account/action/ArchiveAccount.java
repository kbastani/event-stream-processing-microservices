package demo.account.action;

import demo.account.domain.Account;
import demo.account.domain.AccountModule;
import demo.account.domain.AccountService;
import demo.account.domain.AccountStatus;
import demo.account.event.AccountEvent;
import demo.account.event.AccountEventType;
import demo.domain.Action;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.function.Function;

import static demo.account.domain.AccountStatus.ACCOUNT_ACTIVE;
import static demo.account.domain.AccountStatus.ACCOUNT_ARCHIVED;

/**
 * Archives an {@link Account}
 *
 * @author Kenny Bastani
 */
@Service
public class ArchiveAccount extends Action<Account> {

    private final Logger log = Logger.getLogger(this.getClass());

    public Function<Account, Account> getFunction() {
        return (account) -> {
            Assert.isTrue(account.getStatus() != ACCOUNT_ARCHIVED, "The account is already archived");
            Assert.isTrue(account.getStatus() == ACCOUNT_ACTIVE, "An inactive account cannot be archived");
            
            AccountService accountService = account.getModule(AccountModule.class)
                    .getDefaultService();

            AccountStatus status = account.getStatus();

            // Archive the account
            account.setStatus(AccountStatus.ACCOUNT_ARCHIVED);
            account = accountService.update(account);

            try {
                // Trigger the account archived event
                account.sendAsyncEvent(new AccountEvent(AccountEventType.ACCOUNT_ARCHIVED, account));
            } catch (Exception ex) {
                log.error("Account could not be archived", ex);

                // Rollback the operation
                account.setStatus(status);
                accountService.update(account);

                throw ex;
            }

            return account;
        };
    }
}
