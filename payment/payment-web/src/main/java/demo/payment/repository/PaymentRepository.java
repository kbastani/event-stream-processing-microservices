package demo.payment.repository;

import demo.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

/**
 * The repository for managing the persistence of {@link Payment} entities.
 *
 * @author Kenny Bastani
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findPaymentByOrderId(@Param("orderId") Long orderId);
}
