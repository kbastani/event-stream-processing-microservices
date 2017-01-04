package demo.account.action;


import com.fasterxml.jackson.databind.ObjectMapper;
import demo.account.domain.Account;
import demo.domain.Action;
import demo.order.domain.Order;
import org.apache.log4j.Logger;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.client.Traverson;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import static demo.account.domain.AccountStatus.ACCOUNT_ACTIVE;

/**
 * Post a new {@link Order} for an {@link Account}
 *
 * @author Kenny Bastani
 */
@Service
public class PostOrder extends Action<Account> {

    private final Logger log = Logger.getLogger(this.getClass());

    public BiFunction<Account, Order, Order> getFunction() {
        return (account, order) -> {
            Assert.isTrue(account.getStatus() == ACCOUNT_ACTIVE, "Only active accounts can create an order");
            order = order.post();

            try {
                // Create traverson for the new order
                Traverson traverson = new Traverson(URI.create(order.getLink("self")
                        .getHref()), MediaTypes.HAL_JSON);

                Map<String, Object> params = new HashMap<>();
                params.put("accountId", account.getIdentity());

                order = traverson.follow("commands", "connectAccount")
                        .withTemplateParameters(params)
                        .toObject(Order.class);
            } catch (RestClientResponseException ex) {
                log.error("New order could not be posted for the account", ex);
                throw new IllegalStateException(getHttpStatusMessage(ex));
            }

            return order;
        };
    }

    private String getHttpStatusMessage(RestClientResponseException ex) {
        Map<String, String> errorMap = new HashMap<>();
        try {
            errorMap = new ObjectMapper()
                    .readValue(ex.getResponseBodyAsString(), errorMap
                            .getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return errorMap.getOrDefault("message", null);
    }
}
