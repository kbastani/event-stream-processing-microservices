package demo.account.action;

import demo.account.Account;
import demo.account.AccountProvider;
import demo.account.AccountService;
import demo.account.AccountStatus;
import demo.domain.Action;
import demo.account.event.AccountEvent;
import demo.account.event.AccountEventType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.function.Consumer;

import static demo.account.AccountStatus.ACCOUNT_ACTIVE;

/**
 * Suspends an {@link Account}
 *
 * @author Kenny Bastani
 */
@Service
public class SuspendAccount extends Action<Account> {

    public Consumer<Account> getConsumer() {
        return (account) -> {
            Assert.isTrue(account.getStatus() == ACCOUNT_ACTIVE, "An inactive account cannot be suspended");
            
            AccountService accountService = account.getProvider(AccountProvider.class)
                    .getDefaultService();

            // Suspend the account
            account.setStatus(AccountStatus.ACCOUNT_SUSPENDED);
            account = accountService.update(account);

            // Trigger the account suspended event
            account.sendAsyncEvent(new AccountEvent(AccountEventType.ACCOUNT_SUSPENDED, account));
        };
    }
}
