package demo.event;

/**
 * The repository for managing the persistence of {@link PaymentEvent}s.
 *
 * @author Kenny Bastani
 */
public interface PaymentEventRepository extends EventRepository<PaymentEvent, Long> {
}
