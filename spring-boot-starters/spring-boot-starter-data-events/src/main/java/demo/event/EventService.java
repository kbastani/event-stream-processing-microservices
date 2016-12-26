package demo.event;

import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;

import java.io.Serializable;

/**
 * Service interface for managing {@link Event} entities.
 *
 * @author Kenny Bastani
 * @see Event
 * @see Events
 * @see EventServiceImpl
 */
public interface EventService<T extends Event, ID extends Serializable> {

    /**
     * Raises a synchronous domain event. An {@link Event} will be applied to an entity through a chain of HTTP
     * requests/responses.
     *
     * @param event
     * @param links
     * @return the applied {@link Event}
     */
    <E extends ResourceSupport, S extends T> S send(S event, Link... links);

    /**
     * Raises an asynchronous domain event. An {@link Event} will be applied to an entity through a chain of AMQP
     * messages.
     *
     * @param event
     * @param links
     * @return a flag indicating if the {@link Event} message was sent successfully
     */
    <S extends T> Boolean sendAsync(S event, Link... links);

    /**
     * Saves a given event entity. Use the returned instance for further operations as the save operation might have
     * changed the entity instance completely.
     *
     * @param event
     * @return the saved event entity
     */
    <S extends T> S save(S event);

    /**
     * Saves a given event entity. Use the returned instance for further operations as the save operation might have
     * changed the entity instance completely. The {@link ID} parameter is the unique {@link Event} identifier.
     *
     * @param id
     * @param event
     * @return the saved event entity
     */
    <S extends T> S save(ID id, S event);

    /**
     * Retrieves an {@link Event} entity by its id.
     *
     * @param id
     * @return the {@link Event} entity with the given id or {@literal null} if none found
     */
    <EID extends ID> T findOne(EID id);

    /**
     * Retrieves an entity's {@link Event}s by its id.
     *
     * @param entityId
     * @return a {@link Events} containing a collection of {@link Event}s
     */
    <E extends Events> E find(ID entityId);
}
