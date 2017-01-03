package demo.account.action;

import demo.account.domain.Account;
import demo.domain.Action;
import demo.order.domain.OrderModule;
import demo.order.domain.Orders;
import org.springframework.stereotype.Service;

import java.util.function.Function;

/**
 * Query action to get {@link demo.order.domain.Order}s for an an {@link Account}
 *
 * @author Kenny Bastani
 */
@Service
public class GetOrders extends Action<Account> {

    private OrderModule orderModule;

    public GetOrders(OrderModule orderModule) {
        this.orderModule = orderModule;
    }

    public Function<Account, Orders> getFunction() {
        return (account) -> {
            // Get orders from the order service
            return orderModule.getDefaultService()
                    .findOrdersByAccountId(account.getIdentity());
        };
    }
}
