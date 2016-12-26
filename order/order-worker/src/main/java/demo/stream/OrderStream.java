package demo.stream;

import demo.event.EventService;
import demo.event.OrderEvent;
import demo.order.Order;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Profile;

/**
 * The {@link OrderStream} monitors for a variety of {@link OrderEvent} domain
 * events for an {@link Order}.
 *
 * @author kbastani
 */
@EnableAutoConfiguration
@EnableBinding(Sink.class)
@Profile({ "cloud", "development" })
public class OrderStream {

    private EventService eventService;

    public OrderStream(EventService eventService) {
        this.eventService = eventService;
    }

    @StreamListener(Sink.INPUT)
    public void streamListener(OrderEvent orderEvent) {
        eventService.apply(orderEvent);
    }
}
