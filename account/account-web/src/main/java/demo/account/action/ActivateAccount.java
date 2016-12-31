package demo.account.action;

import demo.account.Account;
import demo.account.AccountProvider;
import demo.account.AccountService;
import demo.account.AccountStatus;
import demo.domain.Action;
import demo.event.AccountEvent;
import demo.event.AccountEventType;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.function.Consumer;

import static demo.account.AccountStatus.*;

/**
 * Connects an {@link Account} to an Account.
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

            AccountService accountService = account.getProvider(AccountProvider.class)
                    .getDefaultService();

            // Activate the account
            account.setStatus(AccountStatus.ACCOUNT_ACTIVE);
            account = accountService.update(account);

            // Trigger the account activated event
            account.sendAsyncEvent(new AccountEvent(AccountEventType.ACCOUNT_ACTIVATED, account));
        };
    }
}
