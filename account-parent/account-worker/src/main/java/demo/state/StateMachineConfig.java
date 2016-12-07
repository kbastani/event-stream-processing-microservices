package demo.state;

import demo.account.AccountEventStatus;
import demo.account.AccountEventType;
import demo.command.*;
import demo.event.AccountEvent;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<AccountEventStatus, AccountEventType> {

    final private Logger log = Logger.getLogger(StateMachineConfig.class);

    @Override
    public void configure(StateMachineStateConfigurer<AccountEventStatus, AccountEventType> states)
            throws Exception {
        // Describe initial condition of account status
        states.withStates()
                .initial(AccountEventStatus.ACCOUNT_CREATED)
                .states(EnumSet.allOf(AccountEventStatus.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<AccountEventStatus, AccountEventType> transitions)
            throws Exception {
        // Describe state machine transitions for accounts
        transitions
                .withExternal()
                .source(AccountEventStatus.ACCOUNT_CREATED)
                .target(AccountEventStatus.ACCOUNT_PENDING)
                .event(AccountEventType.ACCOUNT_CREATED)
                .action(createAccount())
                .and()
                .withExternal()
                .source(AccountEventStatus.ACCOUNT_PENDING)
                .target(AccountEventStatus.ACCOUNT_CONFIRMED)
                .event(AccountEventType.ACCOUNT_CONFIRMED)
                .action(confirmAccount())
                .and()
                .withExternal()
                .source(AccountEventStatus.ACCOUNT_CONFIRMED)
                .target(AccountEventStatus.ACCOUNT_ACTIVE)
                .event(AccountEventType.ACCOUNT_ACTIVATED)
                .action(activateAccount())
                .and()
                .withExternal()
                .source(AccountEventStatus.ACCOUNT_ACTIVE)
                .target(AccountEventStatus.ACCOUNT_ARCHIVED)
                .event(AccountEventType.ACCOUNT_ARCHIVED)
                .action(archiveAccount())
                .and()
                .withExternal()
                .source(AccountEventStatus.ACCOUNT_ACTIVE)
                .target(AccountEventStatus.ACCOUNT_SUSPENDED)
                .event(AccountEventType.ACCOUNT_SUSPENDED)
                .action(suspendAccount())
                .and()
                .withExternal()
                .source(AccountEventStatus.ACCOUNT_ARCHIVED)
                .target(AccountEventStatus.ACCOUNT_ACTIVE)
                .event(AccountEventType.ACCOUNT_ACTIVATED)
                .action(unarchiveAccount())
                .and()
                .withExternal()
                .source(AccountEventStatus.ACCOUNT_SUSPENDED)
                .target(AccountEventStatus.ACCOUNT_ACTIVE)
                .event(AccountEventType.ACCOUNT_ACTIVATED)
                .action(unsuspendAccount());
    }

    @Bean
    public Action<AccountEventStatus, AccountEventType> createAccount() {
        return context -> {
            AccountEvent accountEvent = replicateEvent(context);
            if (accountEvent != null) {
                log.info("Executing workflow for created account...");
                try {
                    new CreateAccountCommand(context).apply(accountEvent);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        };
    }

    @Bean
    public Action<AccountEventStatus, AccountEventType> confirmAccount() {
        return context -> {
            AccountEvent accountEvent = replicateEvent(context);
            if (accountEvent != null) {
                log.info("Executing workflow for confirmed account...");
                try {
                    new ConfirmAccountCommand(context).apply(accountEvent);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        };
    }

    @Bean
    public Action<AccountEventStatus, AccountEventType> activateAccount() {
        return context -> {
            AccountEvent accountEvent = replicateEvent(context);
            if (accountEvent != null) {
                log.info("Executing workflow for activated account...");
                try {
                    new ActivateAccountCommand(context).apply(accountEvent);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        };
    }

    @Bean
    public Action<AccountEventStatus, AccountEventType> archiveAccount() {
        return context -> {
            AccountEvent accountEvent = replicateEvent(context);
            if (accountEvent != null) {
                log.info("Executing workflow for archived account...");
                try {
                    new ArchiveAccountCommand(context).apply(accountEvent);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        };
    }

    @Bean
    public Action<AccountEventStatus, AccountEventType> suspendAccount() {
        return context -> {
            AccountEvent accountEvent = replicateEvent(context);
            if (accountEvent != null) {
                log.info("Executing workflow for suspended account...");
                try {
                    new SuspendAccountCommand(context).apply(accountEvent);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        };
    }

    @Bean
    public Action<AccountEventStatus, AccountEventType> unarchiveAccount() {
        return context -> {
            AccountEvent accountEvent = replicateEvent(context);
            if (accountEvent != null) {
                log.info("Executing workflow for unarchived account...");
                try {
                    new UnarchiveAccountCommand(context).apply(accountEvent);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        };
    }

    @Bean
    public Action<AccountEventStatus, AccountEventType> unsuspendAccount() {
        return context -> {
            AccountEvent accountEvent = replicateEvent(context);
            if (accountEvent != null) {
                log.info("Executing workflow for unsuspended account...");
                try {
                    new UnsuspendAccountCommand(context).apply(accountEvent);
                } catch (Exception e) {
                    log.error(e);
                }
            }
        };
    }

    /**
     * Checks the event payload's headers for an {@link AccountEvent} object, which
     * signals to the state machine that the included event has not yet been processed.
     *
     * @param context the state machine context that may include an {@link AccountEvent}
     * @return an {@link AccountEvent} only if this event has not yet been processed, otherwise returns null
     */
    private AccountEvent replicateEvent(StateContext<AccountEventStatus, AccountEventType> context) {
        AccountEvent currentEvent = null;
        log.info(context.getMessage());

        // The state machine is replicated only if an account event is provided in the headers
        if (context.getMessageHeader("event") != null) {
            currentEvent = (AccountEvent) context.getMessageHeader("event");
            log.info("State machine replication completed: " + currentEvent.toString());
        }

        return currentEvent;
    }
}

