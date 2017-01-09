package demo.reservation.config;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBinding(ReservationEventSource.class)
public class ReservationStreamConfig {
}
