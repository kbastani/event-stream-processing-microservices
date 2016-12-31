package demo.domain;

/**
 * A {@link Commodity} object is a {@link Entity} object that is also an {@link Aggregate} root. A commodity object
 * describes all aspects of an aggregate and is both stateless and immutable. A commodity is a locator that connects
 * relationships of a value object to a {@link Provider}.
 * <p>
 * The key difference between a commodity object and an aggregate is that a commodity object is a distributed
 * representation of an aggregate root that combines together references from multiple bounded contexts.
 * <p>
 * Commodities are used by a discovery service to create a reverse-proxy that translates the relationships of an
 * aggregate into URIs.
 *
 * @author Kenny Bastani
 */
public abstract class Commodity extends Aggregate {

}
