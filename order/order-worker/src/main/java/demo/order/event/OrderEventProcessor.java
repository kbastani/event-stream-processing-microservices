package demo.order.event;

import demo.order.StateFactory;
import demo.order.domain.Order;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Profile;

/**
 * The {@link OrderEventProcessor} monitors for a variety of {@link OrderEvent} domain
 * events for an {@link Order}.
 *
 * @author kbastani
 */
@EnableAutoConfiguration
@EnableBinding(Sink.class)
@Profile({"cloud", "development", "docker"})
public class OrderEventProcessor {

    private StateFactory stateFactory;

    public OrderEventProcessor(StateFactory stateFactory) {
        this.stateFactory = stateFactory;
    }

    @StreamListener(Sink.INPUT)
    public void streamListener(OrderEvent orderEvent) {
        stateFactory.apply(orderEvent);
    }
}
