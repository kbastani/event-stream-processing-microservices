package demo.event;

import demo.order.Order;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Profile;

/**
 * The {@link OrderEventStream} monitors for a variety of {@link OrderEvent} domain
 * events for an {@link Order}.
 *
 * @author kbastani
 */
@EnableAutoConfiguration
@EnableBinding(Sink.class)
@Profile({ "cloud", "development" })
public class OrderEventStream {

    private EventService eventService;

    public OrderEventStream(EventService eventService) {
        this.eventService = eventService;
    }

    @StreamListener(Sink.INPUT)
    public void streamListerner(OrderEvent orderEvent) {
        eventService.apply(orderEvent);
    }
}
