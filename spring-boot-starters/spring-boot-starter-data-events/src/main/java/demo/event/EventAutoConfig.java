package demo.event;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * This class auto-configures a {@link BasicEventService} bean.
 *
 * @author Kenny Bastani
 */
@Configuration
@ConditionalOnClass({EventRepository.class, Source.class, RestTemplate.class})
@ConditionalOnMissingBean(EventService.class)
@EnableConfigurationProperties(EventProperties.class)
public class EventAutoConfig {

    private EventRepository eventRepository;
    private Source source;
    private RestTemplate restTemplate;

    public EventAutoConfig(EventRepository eventRepository, Source source, RestTemplate restTemplate) {
        this.eventRepository = eventRepository;
        this.source = source;
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    @Bean
    public EventService eventService() {
        return new BasicEventService(eventRepository, source, restTemplate);
    }
}
