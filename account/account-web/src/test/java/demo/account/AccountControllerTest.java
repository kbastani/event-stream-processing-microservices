package demo.account;

import demo.account.controller.AccountController;
import demo.account.event.AccountEvent;
import demo.account.event.AccountEventType;
import demo.event.EventService;
import demo.event.Events;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(AccountController.class)
@ActiveProfiles("test")
public class AccountControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private EventService<AccountEvent, Long> eventService;

    @Test
    public void getUserAccountResourceShouldReturnAccount() throws Exception {
        String content = "{\"firstName\": \"Jane\", \"lastName\": \"Doe\", \"email\": \"jane.doe@example.com\"}";

        Account account = new Account("Jane", "Doe", "jane.doe@example.com");
        account.setIdentity(1L);

        given(this.accountService.get(1L)).willReturn(account);
        given(this.eventService.find(1L)).willReturn(new Events<>(1L, Collections
                .singletonList(new AccountEvent(AccountEventType
                        .ACCOUNT_CREATED))));

        this.mvc.perform(get("/v1/accounts/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(content));
    }
}
