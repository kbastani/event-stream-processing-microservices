package demo.account.domain;

import demo.domain.Module;
import demo.account.event.AccountEvent;
import demo.event.EventService;

@org.springframework.stereotype.Service
public class AccountModule extends Module<Account> {

    private final AccountService accountService;
    private final EventService<AccountEvent, Long> eventService;

    public AccountModule(AccountService accountService, EventService<AccountEvent, Long> eventService) {
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
