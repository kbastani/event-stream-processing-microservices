package demo.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(path = "events", collectionResourceRel = "events", itemResourceRel = "event")
public interface EventRepository extends JpaRepository<AccountEvent, Long> {
}
