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

import static demo.account.domain.AccountStatus.ACCOUNT_CONFIRMED;
import static demo.account.domain.AccountStatus.ACCOUNT_PENDING;

/**
 * Confirms an {@link Account}
 *
 * @author Kenny Bastani
 */
@Service
public class ConfirmAccount extends Action<Account> {

    private final Logger log = Logger.getLogger(this.getClass());

    public Function<Account, Account> getFunction() {
        return (account) -> {
            Assert.isTrue(account.getStatus() != ACCOUNT_CONFIRMED, "The account has already been confirmed");
            Assert.isTrue(account.getStatus() == ACCOUNT_PENDING, "The account has already been confirmed");

            AccountService accountService = account.getModule(AccountModule.class)
                    .getDefaultService();

            AccountStatus status = account.getStatus();

            // Activate the account
            account.setStatus(AccountStatus.ACCOUNT_CONFIRMED);
            account = accountService.update(account);

            try {
                // Trigger the account confirmed event
                account.sendAsyncEvent(new AccountEvent(AccountEventType.ACCOUNT_CONFIRMED, account));
            } catch (Exception ex) {
                log.error("Account could not be confirmed", ex);

                // Rollback the operation
                account.setStatus(status);
                accountService.update(account);

                throw ex;
            }

            return account;
        };
    }
}
