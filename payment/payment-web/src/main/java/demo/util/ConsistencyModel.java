package demo.util;

/**
 * The {@link ConsistencyModel} is used to configure how an {@link demo.event.Event} will be applied to an entity.
 * BASE is eventually consistent and will process an event workflow asynchronously using Spring Cloud Stream.
 * ACID uses strong eventual consistency and will process an event workflow sequentially and return the result to the
 * calling thread.
 *
 * @author Kenny Bastani
 */
public enum ConsistencyModel {
    BASE,
    ACID
}
