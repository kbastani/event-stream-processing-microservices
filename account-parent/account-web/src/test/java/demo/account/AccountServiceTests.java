package demo.account;

import demo.event.AccountEvent;
import demo.event.AccountEventType;
import demo.event.EventService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
public class AccountServiceTests {

    @MockBean
    private EventService eventService;

    @MockBean
    private AccountRepository accountRepository;

    private AccountService accountService;

    @Before
    public void before() {
        accountService = new AccountService(accountRepository, eventService);
    }

    @Test
    public void getAccountReturnsAccount() throws Exception {
        Account expected = new Account(1L, "123456789", true);
        expected.setUserId(1L);

        given(this.accountRepository.findOne(1L)).willReturn(expected);

        Account actual = accountService.getAccount(1L);

        assertThat(actual).isNotNull();
        assertThat(actual.getUserId()).isEqualTo(1L);
        assertThat(actual.getAccountNumber()).isEqualTo("123456789");
    }

    @Test
    public void createAccountReturnsAccount() throws Exception {
        Account expected = new Account(1L, "123456789", true);
        expected.setUserId(1L);
        Account actual = accountService.createAccount(expected);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(AccountStatus.ACCOUNT_CREATED);
        assertThat(actual.getUserId()).isEqualTo(1L);
        assertThat(actual.getAccountNumber()).isEqualTo("123456789");
    }

    @Test
    public void applyCommandSuspendsAccount() throws Exception {
        Account account = new Account(1L, "123456789", true);
        account.setStatus(AccountStatus.ACCOUNT_ACTIVE);
        account.setUserId(1L);

        AccountEvent accountEvent = new AccountEvent(AccountEventType.ACCOUNT_SUSPENDED);
        accountEvent.setAccount(account);
        accountEvent.setEventId(1L);

        given(this.accountRepository.findOne(1L)).willReturn(account);
        given(this.accountRepository.save(account)).willReturn(account);
        given(this.eventService.createEvent(new AccountEvent(AccountEventType.ACCOUNT_SUSPENDED)))
                .willReturn(accountEvent);

        Account actual = accountService.applyCommand(1L, AccountCommand.SUSPEND_ACCOUNT);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(AccountStatus.ACCOUNT_SUSPENDED);
    }

    @Test
    public void applyCommandUnsuspendsAccount() throws Exception {
        Account account = new Account(1L, "123456789", true);
        account.setStatus(AccountStatus.ACCOUNT_SUSPENDED);
        account.setUserId(1L);

        AccountEvent accountEvent = new AccountEvent(AccountEventType.ACCOUNT_ACTIVATED);
        accountEvent.setAccount(account);
        accountEvent.setEventId(1L);

        given(this.accountRepository.findOne(1L)).willReturn(account);
        given(this.accountRepository.save(account)).willReturn(account);
        given(this.eventService.createEvent(new AccountEvent(AccountEventType.ACCOUNT_ACTIVATED)))
                .willReturn(accountEvent);

        Account actual = accountService.applyCommand(1L, AccountCommand.ACTIVATE_ACCOUNT);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(AccountStatus.ACCOUNT_ACTIVE);
    }

    @Test
    public void applyCommandArchivesAccount() throws Exception {
        Account account = new Account(1L, "123456789", true);
        account.setStatus(AccountStatus.ACCOUNT_ACTIVE);
        account.setUserId(1L);

        AccountEvent accountEvent = new AccountEvent(AccountEventType.ACCOUNT_ARCHIVED);
        accountEvent.setAccount(account);
        accountEvent.setEventId(1L);

        given(this.accountRepository.findOne(1L)).willReturn(account);
        given(this.accountRepository.save(account)).willReturn(account);
        given(this.eventService.createEvent(new AccountEvent(AccountEventType.ACCOUNT_ARCHIVED)))
                .willReturn(accountEvent);

        Account actual = accountService.applyCommand(1L, AccountCommand.ARCHIVE_ACCOUNT);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(AccountStatus.ACCOUNT_ARCHIVED);
    }

    @Test
    public void applyCommmandUnarchivesAccount() throws Exception {
        Account account = new Account(1L, "123456789", true);
        account.setStatus(AccountStatus.ACCOUNT_ARCHIVED);
        account.setUserId(1L);

        AccountEvent accountEvent = new AccountEvent(AccountEventType.ACCOUNT_ACTIVATED);
        accountEvent.setAccount(account);
        accountEvent.setEventId(1L);

        given(this.accountRepository.findOne(1L)).willReturn(account);
        given(this.accountRepository.save(account)).willReturn(account);
        given(this.eventService.createEvent(new AccountEvent(AccountEventType.ACCOUNT_ACTIVATED)))
                .willReturn(accountEvent);

        Account actual = accountService.applyCommand(1L, AccountCommand.ACTIVATE_ACCOUNT);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(AccountStatus.ACCOUNT_ACTIVE);
    }

    @Test
    public void applyCommmandConfirmsAccount() throws Exception {
        Account account = new Account(1L, "123456789", true);
        account.setStatus(AccountStatus.ACCOUNT_PENDING);
        account.setUserId(1L);

        AccountEvent accountEvent = new AccountEvent(AccountEventType.ACCOUNT_CONFIRMED);
        accountEvent.setAccount(account);
        accountEvent.setEventId(1L);

        given(this.accountRepository.findOne(1L)).willReturn(account);
        given(this.accountRepository.save(account)).willReturn(account);
        given(this.eventService.createEvent(new AccountEvent(AccountEventType.ACCOUNT_CONFIRMED)))
                .willReturn(accountEvent);

        Account actual = accountService.applyCommand(1L, AccountCommand.CONFIRM_ACCOUNT);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(AccountStatus.ACCOUNT_CONFIRMED);
    }

    @Test
    public void applyCommmandActivatesAccount() throws Exception {
        Account account = new Account(1L, "123456789", true);
        account.setStatus(AccountStatus.ACCOUNT_CONFIRMED);
        account.setUserId(1L);

        AccountEvent accountEvent = new AccountEvent(AccountEventType.ACCOUNT_ACTIVATED);
        accountEvent.setAccount(account);
        accountEvent.setEventId(1L);

        given(this.accountRepository.findOne(1L)).willReturn(account);
        given(this.accountRepository.save(account)).willReturn(account);
        given(this.eventService.createEvent(new AccountEvent(AccountEventType.ACCOUNT_ACTIVATED)))
                .willReturn(accountEvent);

        Account actual = accountService.applyCommand(1L, AccountCommand.ACTIVATE_ACCOUNT);

        assertThat(actual).isNotNull();
        assertThat(actual.getStatus()).isEqualTo(AccountStatus.ACCOUNT_ACTIVE);
    }
}