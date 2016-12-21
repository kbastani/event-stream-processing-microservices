package demo.state;

import demo.order.OrderStatus;
import demo.event.OrderEventType;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * The {@link StateMachineService} provides factory access to get new state machines for
 * replicating the state of an {@link demo.order.Order} from {@link demo.event.OrderEvents}.
 *
 * @author kbastani
 */
@Service
public class StateMachineService {

    private final StateMachineFactory<OrderStatus, OrderEventType> factory;

    public StateMachineService(StateMachineFactory<OrderStatus, OrderEventType> factory) {
        this.factory = factory;
    }

    /**
     * Create a new state machine that is initially configured and ready for replicating
     * the state of an {@link demo.order.Order} from a sequence of {@link demo.event.OrderEvent}.
     *
     * @return a new instance of {@link StateMachine}
     */
    public StateMachine<OrderStatus, OrderEventType> getStateMachine() {
        // Create a new state machine in its initial state
        StateMachine<OrderStatus, OrderEventType> stateMachine =
                factory.getStateMachine(UUID.randomUUID().toString());

        // Start the new state machine
        stateMachine.start();

        return stateMachine;
    }
}
