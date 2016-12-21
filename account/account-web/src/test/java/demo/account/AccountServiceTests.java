package demo.account;

import demo.event.AccountEvent;
import demo.event.AccountEventType;
import demo.event.EventService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class AccountServiceTests {

    @MockBean
    private EventService eventService;

    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private CacheManager cacheManager;

    private AccountService accountService;

    @Before
    public void before() {
        accountService = new AccountService(accountRepository, eventService, cacheManager);
    }

    @Test
    public void getAccountReturnsAccount() throws Exception {
        Account expected = new Account("Jane", "Doe", "jane.doe@example.com");

        given(this.accountRepository.findOne(1L)).willReturn(expected);

        Account actual = accountService.getAccount(1L);

        assertThat(actual).isNotNull();
        assertThat(actual.getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(actual.getFirstName()).isEqualTo("Jane");
        assertThat(actual.getLastName()).isEqualTo("Doe");
    }

    @Test
    public void createAccountReturnsAccount() throws Exception {
        Account account = new Account("Jane", "Doe", "jane.doe@example.com");
        account.setAccountId(1L);

        given(this.accountRepository.findOne(1L)).willReturn(account);
        given(this.accountRepository.saveAndFlush(account)).willReturn(account);

        Account actual = accountService.createAccount(account);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(AccountStatus.ACCOUNT_CREATED);
        assertThat(actual.getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(actual.getFirstName()).isEqualTo("Jane");
        assertThat(actual.getLastName()).isEqualTo("Doe");
    }

    @Test
    public void applyCommandSuspendsAccount() throws Exception {
        Account account = new Account("Jane", "Doe", "jane.doe@example.com");
        account.setStatus(AccountStatus.ACCOUNT_ACTIVE);

        AccountEvent accountEvent = new AccountEvent(AccountEventType.ACCOUNT_SUSPENDED);
        accountEvent.setAccount(account);
        accountEvent.setEventId(1L);

        given(this.accountRepository.findOne(1L)).willReturn(account);
        given(this.accountRepository.exists(1L)).willReturn(true);
        given(this.accountRepository.save(account)).willReturn(account);
        given(this.eventService.createEvent(1L, new AccountEvent(AccountEventType.ACCOUNT_SUSPENDED)))
                .willReturn(accountEvent);

        Account actual = accountService.applyCommand(1L, AccountCommand.SUSPEND_ACCOUNT);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(AccountStatus.ACCOUNT_SUSPENDED);
    }

    @Test
    public void applyCommandUnsuspendsAccount() throws Exception {
        Account account = new Account("Jane", "Doe", "jane.doe@example.com");
        account.setStatus(AccountStatus.ACCOUNT_SUSPENDED);

        AccountEvent accountEvent = new AccountEvent(AccountEventType.ACCOUNT_ACTIVATED);
        accountEvent.setAccount(account);
        accountEvent.setEventId(1L);

        given(this.accountRepository.findOne(1L)).willReturn(account);
        given(this.accountRepository.exists(1L)).willReturn(true);
        given(this.accountRepository.save(account)).willReturn(account);
        given(this.eventService.createEvent(1L, new AccountEvent(AccountEventType.ACCOUNT_ACTIVATED)))
                .willReturn(accountEvent);

        Account actual = accountService.applyCommand(1L, AccountCommand.ACTIVATE_ACCOUNT);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(AccountStatus.ACCOUNT_ACTIVE);
    }

    @Test
    public void applyCommandArchivesAccount() throws Exception {
        Account account = new Account("Jane", "Doe", "jane.doe@example.com");
        account.setStatus(AccountStatus.ACCOUNT_ACTIVE);

        AccountEvent accountEvent = new AccountEvent(AccountEventType.ACCOUNT_ARCHIVED);
        accountEvent.setAccount(account);
        accountEvent.setEventId(1L);

        given(this.accountRepository.findOne(1L)).willReturn(account);
        given(this.accountRepository.exists(1L)).willReturn(true);
        given(this.accountRepository.save(account)).willReturn(account);
        given(this.eventService.createEvent(1L, new AccountEvent(AccountEventType.ACCOUNT_ARCHIVED)))
                .willReturn(accountEvent);

        Account actual = accountService.applyCommand(1L, AccountCommand.ARCHIVE_ACCOUNT);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(AccountStatus.ACCOUNT_ARCHIVED);
    }

    @Test
    public void applyCommandUnarchivesAccount() throws Exception {
        Account account = new Account("Jane", "Doe", "jane.doe@example.com");
        account.setStatus(AccountStatus.ACCOUNT_ARCHIVED);

        AccountEvent accountEvent = new AccountEvent(AccountEventType.ACCOUNT_ACTIVATED);
        accountEvent.setAccount(account);
        accountEvent.setEventId(1L);

        given(this.accountRepository.findOne(1L)).willReturn(account);
        given(this.accountRepository.exists(1L)).willReturn(true);
        given(this.accountRepository.save(account)).willReturn(account);
        given(this.eventService.createEvent(1L, new AccountEvent(AccountEventType.ACCOUNT_ACTIVATED)))
                .willReturn(accountEvent);

        Account actual = accountService.applyCommand(1L, AccountCommand.ACTIVATE_ACCOUNT);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(AccountStatus.ACCOUNT_ACTIVE);
    }

    @Test
    public void applyCommandConfirmsAccount() throws Exception {
        Account account = new Account("Jane", "Doe", "jane.doe@example.com");
        account.setStatus(AccountStatus.ACCOUNT_PENDING);

        AccountEvent accountEvent = new AccountEvent(AccountEventType.ACCOUNT_CONFIRMED);
        accountEvent.setAccount(account);
        accountEvent.setEventId(1L);

        given(this.accountRepository.findOne(1L)).willReturn(account);
        given(this.accountRepository.exists(1L)).willReturn(true);
        given(this.accountRepository.save(account)).willReturn(account);
        given(this.eventService.createEvent(1L, new AccountEvent(AccountEventType.ACCOUNT_CONFIRMED)))
                .willReturn(accountEvent);

        Account actual = accountService.applyCommand(1L, AccountCommand.CONFIRM_ACCOUNT);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(AccountStatus.ACCOUNT_CONFIRMED);
    }

    @Test
    public void applyCommandActivatesAccount() throws Exception {
        Account account = new Account("Jane", "Doe", "jane.doe@example.com");
        account.setStatus(AccountStatus.ACCOUNT_CONFIRMED);


        AccountEvent accountEvent = new AccountEvent(AccountEventType.ACCOUNT_ACTIVATED);
        accountEvent.setAccount(account);
        accountEvent.setEventId(1L);

        given(this.accountRepository.findOne(1L)).willReturn(account);
        given(this.accountRepository.exists(1L)).willReturn(true);
        given(this.accountRepository.save(account)).willReturn(account);
        given(this.eventService.createEvent(1L, new AccountEvent(AccountEventType.ACCOUNT_ACTIVATED)))
                .willReturn(accountEvent);

        Account actual = accountService.applyCommand(1L, AccountCommand.ACTIVATE_ACCOUNT);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(AccountStatus.ACCOUNT_ACTIVE);
    }
}