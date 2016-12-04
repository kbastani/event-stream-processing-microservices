package demo.event;

import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBinding(ProducerChannels.class)
public class StreamConfig {

}
