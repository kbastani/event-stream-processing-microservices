package demo.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<OrderEvent, Long> {
    Page<OrderEvent> findOrderEventsByOrderId(@Param("orderId") Long orderId, Pageable pageable);
}
