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

import java.util.Arrays;
import java.util.function.Consumer;

import static demo.account.domain.AccountStatus.*;

/**
 * Activates an {@link Account}
 *
 * @author Kenny Bastani
 */
@Service
public class ActivateAccount extends Action<Account> {

    public Consumer<Account> getConsumer() {
        return (account) -> {
            Assert.isTrue(account.getStatus() != ACCOUNT_ACTIVE, "The account is already active");
            Assert.isTrue(Arrays.asList(ACCOUNT_CONFIRMED, ACCOUNT_SUSPENDED, ACCOUNT_ARCHIVED)
                    .contains(account.getStatus()), "The account cannot be activated");

            AccountService accountService = account.getModule(AccountModule.class)
                    .getDefaultService();

            // Activate the account
            account.setStatus(AccountStatus.ACCOUNT_ACTIVE);
            account = accountService.update(account);

            // Trigger the account activated event
            account.sendAsyncEvent(new AccountEvent(AccountEventType.ACCOUNT_ACTIVATED, account));
        };
    }
}
