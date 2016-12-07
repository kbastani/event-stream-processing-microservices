package demo.account;

import demo.event.AccountEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/v1")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping(path = "/accounts")
    public ResponseEntity createAccount(@RequestBody Account account) {
        return Optional.ofNullable(accountService.createAccountResource(account))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new IllegalArgumentException("Account creation failed"));
    }

    @PutMapping(path = "/accounts/{id}")
    public ResponseEntity updateAccount(@RequestBody Account account, @PathVariable Long id) {
        return Optional.ofNullable(accountService.updateAccountResource(id, account))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new IllegalArgumentException("Account update failed"));
    }

    @GetMapping(path = "/accounts/{id}")
    public ResponseEntity getAccount(@PathVariable Long id) {
        return Optional.ofNullable(accountService.getAccountResource(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping(path = "/accounts/{id}/events")
    public ResponseEntity createAccount(@PathVariable Long id, @RequestBody AccountEvent event) {
        return Optional.ofNullable(accountService.appendEventResource(id, event))
                .map(e -> new ResponseEntity<>(e, HttpStatus.CREATED))
                .orElseThrow(() -> new IllegalArgumentException("Append account event failed"));
    }

    @GetMapping(path = "/accounts/{id}/commands")
    public ResponseEntity getAccountCommands(@PathVariable Long id) {
        return Optional.ofNullable(accountService.getCommandsResource(id))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new IllegalArgumentException("The account could not be found"));
    }

    @GetMapping(path = "/accounts/{id}/commands/confirm")
    public ResponseEntity confirmAccount(@PathVariable Long id) {
        return Optional.ofNullable(accountService.applyCommand(id, AccountCommand.CONFIRM_ACCOUNT))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new IllegalArgumentException("The command could not be applied"));
    }

    @GetMapping(path = "/accounts/{id}/commands/activate")
    public ResponseEntity activateAccount(@PathVariable Long id) {
        return Optional.ofNullable(accountService.applyCommand(id, AccountCommand.ACTIVATE_ACCOUNT))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new IllegalArgumentException("The command could not be applied"));
    }

    @GetMapping(path = "/accounts/{id}/commands/suspend")
    public ResponseEntity suspendAccount(@PathVariable Long id) {
        return Optional.ofNullable(accountService.applyCommand(id, AccountCommand.SUSPEND_ACCOUNT))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new IllegalArgumentException("The command could not be applied"));
    }

    @GetMapping(path = "/accounts/{id}/commands/archive")
    public ResponseEntity archiveAccount(@PathVariable Long id) {
        return Optional.ofNullable(accountService.applyCommand(id, AccountCommand.ARCHIVE_ACCOUNT))
                .map(e -> new ResponseEntity<>(e, HttpStatus.OK))
                .orElseThrow(() -> new IllegalArgumentException("The command could not be applied"));
    }
}
