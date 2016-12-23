package demo.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<PaymentEvent, Long> {
    Page<PaymentEvent> findPaymentEventsByPaymentId(@Param("paymentId") Long paymentId, Pageable pageable);
}
