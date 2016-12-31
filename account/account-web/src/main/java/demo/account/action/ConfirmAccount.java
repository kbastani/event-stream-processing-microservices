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

import static demo.account.AccountStatus.ACCOUNT_PENDING;

/**
 * Confirms an {@link Account}
 *
 * @author Kenny Bastani
 */
@Service
public class ConfirmAccount extends Action<Account> {

    public Consumer<Account> getConsumer() {
        return (account) -> {
            Assert.isTrue(account.getStatus() == ACCOUNT_PENDING, "The account has already been confirmed");

            AccountService accountService = account.getProvider(AccountProvider.class)
                    .getDefaultService();

            // Confirm the account
            account.setStatus(AccountStatus.ACCOUNT_CONFIRMED);
            account = accountService.update(account);

            // Trigger the account confirmed
            account.sendAsyncEvent(new AccountEvent(AccountEventType.ACCOUNT_CONFIRMED, account));
        };
    }
}
