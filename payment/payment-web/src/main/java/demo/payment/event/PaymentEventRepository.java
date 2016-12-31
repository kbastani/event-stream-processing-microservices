package demo.payment.event;

import demo.event.EventRepository;

/**
 * The repository for managing the persistence of {@link PaymentEvent}s.
 *
 * @author Kenny Bastani
 */
public interface PaymentEventRepository extends EventRepository<PaymentEvent, Long> {
}
