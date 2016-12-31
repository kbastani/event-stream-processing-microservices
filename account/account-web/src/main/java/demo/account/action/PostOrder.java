package demo.account.action;


import demo.account.Account;
import demo.domain.Action;
import demo.order.domain.Order;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static demo.account.AccountStatus.ACCOUNT_ACTIVE;

/**
 * Confirms an {@link Account}
 *
 * @author Kenny Bastani
 */
@Service
public class PostOrder extends Action<Account> {

    public BiFunction<Account, Order, Account> getFunction() {
        return (account, order) -> {
            Assert.isTrue(account.getStatus() == ACCOUNT_ACTIVE, "Only active accounts can create an order");
            order = order.post();

            // Create traverson for the new order
            Traverson traverson = new Traverson(URI.create(order.getLink("self")
                    .getHref()), MediaTypes.HAL_JSON);

            Map<String, Object> params = new HashMap<>();
            params.put("accountId", account.getIdentity());

            order = traverson.follow("commands", "connectAccount")
                    .withTemplateParameters(params)
                    .toObject(Order.class);

            return account;
        };
    }
}
