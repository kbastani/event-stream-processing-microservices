package demo.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.account.Account;
import demo.account.AccountController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * The {@link AccountEvents} is a hypermedia collection of {@link AccountEvent} resources.
 *
 * @author kbastani
 */
public class AccountEvents extends Resources<AccountEvent> {

    private Long accountId;

    /**
     * Create a new {@link AccountEvents} hypermedia resources collection for an {@link Account}.
     *
     * @param accountId is the unique identifier for the {@link Account}
     * @param content   is the collection of {@link AccountEvents} attached to the {@link Account}
     */
    public AccountEvents(Long accountId, Iterable<AccountEvent> content) {
        this(content);
        this.accountId = accountId;

        // Add hypermedia links to resources parent
        add(linkTo(AccountController.class)
                        .slash("accounts")
                        .slash(accountId)
                        .slash("events")
                        .withSelfRel(),
                linkTo(AccountController.class)
                        .slash("accounts")
                        .slash(accountId)
                        .withRel("account"));

        // Add hypermedia links to each item of the collection
        content.forEach(event -> event.add(
                linkTo(EventController.class)
                        .slash("events")
                        .slash(event.getEventId())
                        .withSelfRel()
        ));
    }

    /**
     * Creates a {@link Resources} instance with the given content and {@link Link}s (optional).
     *
     * @param content must not be {@literal null}.
     * @param links   the links to be added to the {@link Resources}.
     */
    private AccountEvents(Iterable<AccountEvent> content, Link... links) {
        super(content, links);
    }

    /**
     * Get the {@link Account} identifier that the {@link AccountEvents} apply to.
     *
     * @return the account identifier
     */
    @JsonIgnore
    public Long getAccountId() {
        return accountId;
    }
}
