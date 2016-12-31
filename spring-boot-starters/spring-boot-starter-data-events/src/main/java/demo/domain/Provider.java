package demo.domain;

import demo.event.EventService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * A {@link Provider} is a collection of {@link Service} references that are used to consume and/or produce
 * {@link org.springframework.hateoas.Resource}s. Providers transfer a resource into a {@link Commodity}.
 *
 * @author Kenny Bastani
 */
@Component
public abstract class Provider<T extends Aggregate> implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        Provider.applicationContext = applicationContext;
    }

    public abstract Service<?, ?> getDefaultService();

    public abstract EventService<?, ?> getDefaultEventService();
}
