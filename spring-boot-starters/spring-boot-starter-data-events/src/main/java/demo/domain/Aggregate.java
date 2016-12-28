package demo.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.hateoas.*;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

/**
 * An {@link Aggregate} is an entity that contains references to one or more other {@link Value} objects. Aggregates
 * may contain a collection of references to a {@link Command}. All command references on an aggregate should be
 * explicitly typed.
 *
 * @author Kenny Bastani
 */
public abstract class Aggregate<ID extends Serializable> extends ResourceSupport implements Value<Link> {

    @JsonProperty("id")
    abstract ID getIdentity();

    private final ApplicationContext applicationContext = Optional.ofNullable(Provider.getApplicationContext())
            .orElse(null);

    /**
     * Retrieves an {@link Action} for this {@link Provider}
     *
     * @return the action for this provider
     * @throws IllegalArgumentException if the application context is unavailable or the provider does not exist
     */
    @SuppressWarnings("unchecked")
    protected <T extends Action<A>, A extends Aggregate> T getAction(
            Class<T> actionType) throws IllegalArgumentException {
        Provider provider = getProvider();
        Service service = provider.getDefaultService();
        return (T) service.getAction(actionType);
    }

    /**
     * Retrieves an instance of the {@link Provider} for this instance
     *
     * @return the provider for this instance
     * @throws IllegalArgumentException if the application context is unavailable or the provider does not exist
     */
    @SuppressWarnings("unchecked")
    protected <T extends Provider<A>, A extends Aggregate> T getProvider() throws IllegalArgumentException {
        return getProvider((Class<T>) ResolvableType
                .forClassWithGenerics(Provider.class, ResolvableType.forInstance(this))
                .getRawClass());
    }

    /**
     * Retrieves an instance of a {@link Provider} with the supplied type
     *
     * @return an instance of the requested {@link Provider}
     * @throws IllegalArgumentException if the application context is unavailable or the provider does not exist
     */
    protected <T extends Provider<A>, A extends Aggregate> T getProvider(
            Class<T> providerType) throws IllegalArgumentException {
        Assert.notNull(applicationContext, "The application context is unavailable");
        T provider = applicationContext.getBean(providerType);
        Assert.notNull(provider, "The requested provider is not registered in the application context");
        return provider;
    }

    @Override
    public List<Link> getLinks() {
        List<Link> links = super.getLinks()
                .stream()
                .collect(Collectors.toList());

        links.add(getId());

        return links;
    }

    @JsonIgnore
    @SuppressWarnings("unchecked")
    public CommandResources getCommands() {
        CommandResources commandResources = new CommandResources();

        // Get command annotations on the aggregate
        List<Command> commands = Arrays.stream(this.getClass()
                .getMethods())
                .filter(a -> a.isAnnotationPresent(Command.class))
                .map(a -> a.getAnnotation(Command.class))
                .collect(Collectors.toList());

        // Compile the collection of command links
        List<Link> commandLinks = commands.stream()
                .map(a -> Arrays.stream(ReflectionUtils.getAllDeclaredMethods(a.controller()))
                        .filter(m -> m.getName()
                                .equalsIgnoreCase(a.method()))
                        .findFirst()
                        .orElseGet(null))
                .map(m -> {
                    String uri = linkTo(m, getIdentity()).withRel(m.getName())
                            .getHref();

                    return new Link(new UriTemplate(uri, new TemplateVariables(Arrays.stream(m.getParameters())
                            .filter(p -> p.isAnnotationPresent(RequestParam.class))
                            .map(p -> new TemplateVariable(p.getAnnotation(RequestParam.class)
                                    .value(), TemplateVariable.VariableType.REQUEST_PARAM))
                            .toArray(TemplateVariable[]::new))), m.getName());
                })
                .collect(Collectors.toList());

        commandResources.add(commandLinks);

        return commandResources;
    }

    public static class CommandResources extends ResourceSupport {
    }
}
