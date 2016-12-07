package demo.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.event.AccountEvent;
import demo.event.EventController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resources;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

public class AccountEvents extends Resources<AccountEvent> {

    private Long accountId;

    /**
     * Creates an empty {@link Resources} instance.
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
    public AccountEvents(Iterable<AccountEvent> content, Link... links) {
        super(content, links);
    }

    @JsonIgnore
    public Long getAccountId() {
        return accountId;
    }
}
