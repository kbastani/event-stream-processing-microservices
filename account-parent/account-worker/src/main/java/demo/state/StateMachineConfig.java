package demo.state;

import demo.account.AccountEventStatus;
import demo.account.AccountEventType;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        return context -> log.info(context.getMessage());
    }

    @Bean
    public Action<AccountEventStatus, AccountEventType> confirmAccount() {
        return context -> log.info(context.getMessage());
    }

    @Bean
    public Action<AccountEventStatus, AccountEventType> activateAccount() {
        return context -> log.info(context.getMessage());
    }

    @Bean
    public Action<AccountEventStatus, AccountEventType> archiveAccount() {
        return context -> log.info(context.getMessage());
    }

    @Bean
    public Action<AccountEventStatus, AccountEventType> suspendAccount() {
        return context -> log.info(context.getMessage());
    }

    @Bean
    public Action<AccountEventStatus, AccountEventType> unarchiveAccount() {
        return context -> log.info(context.getMessage());
    }

    @Bean
    public Action<AccountEventStatus, AccountEventType> unsuspendAccount() {
        return context -> log.info(context.getMessage());
    }
}

