package demo.domain;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * An {@link Action} is a reference of a method. A function contains an address to the location of a method. A function
 * may contain meta-data that describes the inputs and outputs of a method. An action invokes a method annotated with
 * {@link Command}.
 *
 * @author Kenny Bastani
 */
@Component
public abstract class Action<A extends Aggregate> implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
