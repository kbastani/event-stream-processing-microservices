package demo.state;

import demo.account.AccountEventStatus;
import demo.account.AccountEventType;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StateMachineService {

    private final StateMachineFactory<AccountEventStatus, AccountEventType> factory;

    public StateMachineService(StateMachineFactory<AccountEventStatus, AccountEventType> factory) {
        this.factory = factory;
    }

    public StateMachine<AccountEventStatus, AccountEventType> getStateMachine() {
        StateMachine<AccountEventStatus, AccountEventType> stateMachine = factory.getStateMachine(UUID.randomUUID().toString());
        stateMachine.start();
        return stateMachine;
    }
}
