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

import static demo.account.domain.AccountStatus.ACCOUNT_ACTIVE;
import static demo.account.domain.AccountStatus.ACCOUNT_ARCHIVED;

/**
 * Archives an {@link Account}
 *
 * @author Kenny Bastani
 */
@Service
public class ArchiveAccount extends Action<Account> {

    public Consumer<Account> getConsumer() {
        return (account) -> {
            Assert.isTrue(account.getStatus() != ACCOUNT_ARCHIVED, "The account is already archived");
            Assert.isTrue(account.getStatus() == ACCOUNT_ACTIVE, "An inactive account cannot be archived");
            
            AccountService accountService = account.getModule(AccountModule.class)
                    .getDefaultService();

            // Archive the account
            account.setStatus(AccountStatus.ACCOUNT_ARCHIVED);
            account = accountService.update(account);

            // Trigger the account archived event
            account.sendEvent(new AccountEvent(AccountEventType.ACCOUNT_ARCHIVED, account));
        };
    }
}
