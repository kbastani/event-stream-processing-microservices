package demo.domain;


import org.springframework.hateoas.Identifiable;

import java.io.Serializable;

/**
 * {@link Value} objects are wrappers that contain the serializable properties that uniquely identify an entity.
 * Value objects contain a collection of relationships. Value objects contain a collection of comparison operators.
 * The default identity comparator evaluates true if the compared objects have the same identifier.
 *
 * @author Kenny Bastani
 */
public interface Value<ID extends Serializable> extends Identifiable<ID> {
}
