package demo.event;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

public interface Sink {
    String INPUT = "input";

    @Input(INPUT)
    SubscribableChannel input();
}
