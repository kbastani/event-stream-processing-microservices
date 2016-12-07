package demo.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.account.Account;
import demo.account.AccountEventType;
import demo.domain.BaseEntity;
import demo.log.Log;
import org.springframework.data.rest.core.annotation.RestResource;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * The domain event {@link AccountEvent} tracks the type and state of events as
 * applied to the {@link Account} domain object. This event resource can be used
 * to event source the aggregate state of {@link Account}.
 * <p>
 * This event resource also provides a transaction log that can be used to append
 * actions to the event. The collection of {@link Log} items can be used to remediate
 * partial failures.
 *
 * @author kbastani
 */
@Entity
@RestResource(path = "events", rel = "events")
public class AccountEvent extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private AccountEventType type;

    @OneToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Account account;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Log> logs = new HashSet<>();

    public AccountEvent() {
    }

    public AccountEvent(AccountEventType type) {
        this.type = type;
    }

    @JsonIgnore
    public Long getEventId() {
        return id;
    }

    public void setEventId(Long id) {
        this.id = id;
    }

    public AccountEventType getType() {
        return type;
    }

    public void setType(AccountEventType type) {
        this.type = type;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Set<Log> getLogs() {
        return logs;
    }

    public void setLogs(Set<Log> logs) {
        this.logs = logs;
    }

    @Override
    public String toString() {
        return "AccountEvent{" +
                "id=" + id +
                ", type=" + type +
                ", account=" + account +
                ", logs=" + logs +
                "} " + super.toString();
    }
}
