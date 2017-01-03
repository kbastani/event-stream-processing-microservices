package demo.account.action;

import demo.account.domain.Account;
import demo.account.domain.AccountModule;
import demo.account.domain.AccountService;
import demo.account.domain.AccountStatus;
import demo.domain.Action;
import demo.account.event.AccountEvent;
import demo.account.event.AccountEventType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.function.Consumer;

import static demo.account.domain.AccountStatus.ACCOUNT_PENDING;

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

            AccountService accountService = account.getModule(AccountModule.class)
                    .getDefaultService();

            // Confirm the account
            account.setStatus(AccountStatus.ACCOUNT_CONFIRMED);
            account = accountService.update(account);

            // Trigger the account confirmed
            account.sendEvent(new AccountEvent(AccountEventType.ACCOUNT_CONFIRMED, account));
        };
    }
}
