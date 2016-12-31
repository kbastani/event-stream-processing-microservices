package demo.account;

import demo.domain.Provider;
import demo.event.AccountEvent;
import demo.event.EventService;

@org.springframework.stereotype.Service
public class AccountProvider extends Provider<Account> {

    private final AccountService accountService;
    private final EventService<AccountEvent, Long> eventService;

    public AccountProvider(AccountService accountService, EventService<AccountEvent, Long> eventService) {
        this.accountService = accountService;
        this.eventService = eventService;
    }

    public AccountService getAccountService() {
        return accountService;
    }

    public EventService<AccountEvent, Long> getEventService() {
        return eventService;
    }

    @Override
    public AccountService getDefaultService() {
        return accountService;
    }

    @Override
    public EventService<AccountEvent, Long> getDefaultEventService() {
        return eventService;
    }
}
