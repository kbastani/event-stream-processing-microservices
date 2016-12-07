package demo.log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import demo.account.AccountEventType;
import demo.domain.BaseEntity;

import javax.persistence.*;

/**
 * The {@link Log} entity is used to track {@link demo.event.AccountEvent} actions.
 */
@Entity
public class Log extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    private AccountEventType action;

    public Log() {
    }

    public Log(AccountEventType action) {
        this.action = action;
    }

    @JsonIgnore
    public Long getLogId() {
        return id;
    }

    public void setLogId(Long id) {
        this.id = id;
    }

    public AccountEventType getAction() {
        return action;
    }

    public void setAction(AccountEventType action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "Log{" +
                "id=" + id +
                ", action=" + action +
                "} " + super.toString();
    }
}
