package demo.account.action;

import demo.account.Account;
import demo.domain.Action;
import demo.order.OrderProvider;
import demo.order.domain.Orders;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class GetOrders extends Action<Account> {

    private OrderProvider orderProvider;

    public GetOrders(OrderProvider orderProvider) {
        this.orderProvider = orderProvider;
    }

    public Function<Account, Orders> getFunction() {
        return (account) -> {
            // Get orders from the order service
            return orderProvider.getDefaultService()
                    .findOrdersByAccountId(account.getIdentity());
        };
    }
}
