package demo.account.event;

import demo.event.EventRepository;

public interface AccountEventRepository extends EventRepository<AccountEvent, Long> {
}
