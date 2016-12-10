package demo.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.account.Account;
import demo.domain.BaseEntity;

import javax.persistence.*;

/**
 * The domain event {@link AccountEvent} tracks the type and state of events as
 * applied to the {@link Account} domain object. This event resource can be used
 * to event source the aggregate state of {@link Account}.
 * <p>
 * This event resource also provides a transaction log that can be used to append
 * actions to the event.
 *
 * @author kbastani
 */
@Entity
public class AccountEvent extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private AccountEventType type;

    @OneToOne(cascade = CascadeType.MERGE, fetch = FetchType.LAZY)
    @JsonIgnore
    private Account account;

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

    @Override
    public String toString() {
        return "AccountEvent{" +
                "id=" + id +
                ", type=" + type +
                ", account=" + account +
                "} " + super.toString();
    }
}
