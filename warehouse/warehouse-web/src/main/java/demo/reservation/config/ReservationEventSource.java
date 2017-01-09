package demo.reservation.config;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.messaging.MessageChannel;

public interface ReservationEventSource extends Source {
    String OUTPUT = "reservation";

    @Override
    @Output(ReservationEventSource.OUTPUT)
    MessageChannel output();
}
