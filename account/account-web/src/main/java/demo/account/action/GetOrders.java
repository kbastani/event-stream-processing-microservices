package demo.account.action;

import demo.account.domain.Account;
import demo.domain.Action;
import demo.order.domain.OrderModule;
import demo.order.domain.Orders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Query action to get {@link demo.order.domain.Order}s for an an {@link Account}
 *
 * @author Kenny Bastani
 */
@Service
@Transactional
public class GetOrders extends Action<Account> {

    private OrderModule orderModule;

    public GetOrders(OrderModule orderModule) {
        this.orderModule = orderModule;
    }

    public Orders apply(Account account) {
        // Get orders from the order service
        return orderModule.getDefaultService()
                .findOrdersByAccountId(account.getIdentity());
    }
}
