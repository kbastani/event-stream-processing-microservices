package demo.account;

import demo.event.AccountEvent;
import demo.event.AccountEvents;
import demo.event.EventController;
import demo.event.EventService;
import org.springframework.hateoas.LinkBuilder;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping("/v1")
public class AccountController {

    private final AccountService accountService;
    private final EventService eventService;

    public AccountController(AccountService accountService, EventService eventService) {
        this.accountService = accountService;
        this.eventService = eventService;
    }

    @PostMapping(path = "/accounts")
    public ResponseEntity createAccount(@RequestBody Account account) {
        return Optional.ofNullable(createAccountResource(account))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Account creation failed"));
    }

    @PutMapping(path = "/accounts/{id}")
    public ResponseEntity updateAccount(@RequestBody Account account, @PathVariable Long id) {
        return Optional.ofNullable(updateAccountResource(id, account))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Account update failed"));
    }

    @GetMapping(path = "/accounts/{id}")
    public ResponseEntity getAccount(@PathVariable Long id) {
        return Optional.ofNullable(getAccountResource(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(path = "/accounts/{id}")
    public ResponseEntity deleteAccount(@PathVariable Long id) {
        return Optional.ofNullable(accountService.deleteAccount(id))
                .map(e -> new ResponseEntity<>(HttpStatus.NO_CONTENT))
                .orElseThrow(() -> new RuntimeException("Account deletion failed"));
    }

    @GetMapping(path = "/accounts/{id}/events")
    public ResponseEntity getAccountEvents(@PathVariable Long id) {
        return Optional.of(getAccountEventResources(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("Could not get account events"));
    }

    @PostMapping(path = "/accounts/{id}/events")
    public ResponseEntity createAccount(@PathVariable Long id, @RequestBody AccountEvent event) {
        return Optional.ofNullable(appendEventResource(id, event))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new RuntimeException("Append account event failed"));
    }

    @GetMapping(path = "/accounts/{id}/commands")
    public ResponseEntity getAccountCommands(@PathVariable Long id) {
        return Optional.ofNullable(getCommandsResource(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The account could not be found"));
    }

    @GetMapping(path = "/accounts/{id}/commands/confirm")
    public ResponseEntity confirmAccount(@PathVariable Long id) {
        return Optional.ofNullable(getAccountResource(
                accountService.applyCommand(id, AccountCommand.CONFIRM_ACCOUNT)))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @GetMapping(path = "/accounts/{id}/commands/activate")
    public ResponseEntity activateAccount(@PathVariable Long id) {
        return Optional.ofNullable(getAccountResource(
                accountService.applyCommand(id, AccountCommand.ACTIVATE_ACCOUNT)))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @GetMapping(path = "/accounts/{id}/commands/suspend")
    public ResponseEntity suspendAccount(@PathVariable Long id) {
        return Optional.ofNullable(getAccountResource(
                accountService.applyCommand(id, AccountCommand.SUSPEND_ACCOUNT)))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    @GetMapping(path = "/accounts/{id}/commands/archive")
    public ResponseEntity archiveAccount(@PathVariable Long id) {
        return Optional.ofNullable(getAccountResource(
                accountService.applyCommand(id, AccountCommand.ARCHIVE_ACCOUNT)))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new RuntimeException("The command could not be applied"));
    }

    /**
     * Retrieves a hypermedia resource for {@link Account} with the specified identifier.
     *
     * @param id is the unique identifier for looking up the {@link Account} entity
     * @return a hypermedia resource for the fetched {@link Account}
     */
    private Resource<Account> getAccountResource(Long id) {
        Resource<Account> accountResource = null;

        // Get the account for the provided id
        Account account = accountService.getAccount(id);

        // If the account exists, wrap the hypermedia response
        if (account != null)
            accountResource = getAccountResource(account);


        return accountResource;
    }

    /**
     * Creates a new {@link Account} entity and persists the result to the repository.
     *
     * @param account is the {@link Account} model used to create a new account
     * @return a hypermedia resource for the newly created {@link Account}
     */
    private Resource<Account> createAccountResource(Account account) {
        Assert.notNull(account, "Account body must not be null");
        Assert.notNull(account.getEmail(), "Email is required");
        Assert.notNull(account.getFirstName(), "First name is required");
        Assert.notNull(account.getLastName(), "Last name is required");

        // Create the new account
        account = accountService.registerAccount(account);

        return getAccountResource(account);
    }

    /**
     * Update a {@link Account} entity for the provided identifier.
     *
     * @param id      is the unique identifier for the {@link Account} update
     * @param account is the entity representation containing any updated {@link Account} fields
     * @return a hypermedia resource for the updated {@link Account}
     */
    private Resource<Account> updateAccountResource(Long id, Account account) {
        return getAccountResource(accountService.updateAccount(id, account));
    }

    /**
     * Appends an {@link AccountEvent} domain event to the event log of the {@link Account}
     * aggregate with the specified accountId.
     *
     * @param accountId is the unique identifier for the {@link Account}
     * @param event     is the {@link AccountEvent} that attempts to alter the state of the {@link Account}
     * @return a hypermedia resource for the newly appended {@link AccountEvent}
     */
    private Resource<AccountEvent> appendEventResource(Long accountId, AccountEvent event) {
        Resource<AccountEvent> eventResource = null;

        event = accountService.appendEvent(accountId, event);

        if (event != null) {
            eventResource = new Resource<>(event,
                    linkTo(EventController.class)
                            .slash("events")
                            .slash(event.getEventId())
                            .withSelfRel(),
                    linkTo(AccountController.class)
                            .slash("accounts")
                            .slash(accountId)
                            .withRel("account")
            );
        }

        return eventResource;
    }

    /**
     * Get the {@link AccountCommand} hypermedia resource that lists the available commands that can be applied
     * to an {@link Account} entity.
     *
     * @param id is the {@link Account} identifier to provide command links for
     * @return an {@link AccountCommandsResource} with a collection of embedded command links
     */
    private AccountCommandsResource getCommandsResource(Long id) {
        // Get the account resource for the identifier
        Resource<Account> accountResource = getAccountResource(id);

        // Create a new account commands hypermedia resource
        AccountCommandsResource commandResource = new AccountCommandsResource();

        // Add account command hypermedia links
        if (accountResource != null) {
            commandResource.add(
                    getCommandLinkBuilder(id)
                            .slash("confirm")
                            .withRel("confirm"),
                    getCommandLinkBuilder(id)
                            .slash("activate")
                            .withRel("activate"),
                    getCommandLinkBuilder(id)
                            .slash("suspend")
                            .withRel("suspend"),
                    getCommandLinkBuilder(id)
                            .slash("archive")
                            .withRel("archive")
            );
        }

        return commandResource;
    }

    /**
     * Get {@link AccountEvents} for the supplied {@link Account} identifier.
     *
     * @param id is the unique identifier of the {@link Account}
     * @return a list of {@link AccountEvent} wrapped in a hypermedia {@link AccountEvents} resource
     */
    private AccountEvents getAccountEventResources(Long id) {
        return new AccountEvents(id, eventService.getAccountEvents(id));
    }

    /**
     * Generate a {@link LinkBuilder} for generating the {@link AccountCommandsResource}.
     *
     * @param id is the unique identifier for a {@link Account}
     * @return a {@link LinkBuilder} for the {@link AccountCommandsResource}
     */
    private LinkBuilder getCommandLinkBuilder(Long id) {
        return linkTo(AccountController.class)
                .slash("accounts")
                .slash(id)
                .slash("commands");
    }

    /**
     * Get a hypermedia enriched {@link Account} entity.
     *
     * @param account is the {@link Account} to enrich with hypermedia links
     * @return is a hypermedia enriched resource for the supplied {@link Account} entity
     */
    private Resource<Account> getAccountResource(Account account) {
        Resource<Account> accountResource;

        // Prepare hypermedia response
        accountResource = new Resource<>(account,
                linkTo(AccountController.class)
                        .slash("accounts")
                        .slash(account.getAccountId())
                        .withSelfRel(),
                linkTo(AccountController.class)
                        .slash("accounts")
                        .slash(account.getAccountId())
                        .slash("events")
                        .withRel("events"),
                getCommandLinkBuilder(account.getAccountId())
                        .withRel("commands")
        );

        return accountResource;
    }
}
